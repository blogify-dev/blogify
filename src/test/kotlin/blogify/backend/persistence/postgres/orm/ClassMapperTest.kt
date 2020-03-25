package blogify.backend.persistence.postgres.orm

import blogify.backend.annotations.Invisible
import blogify.backend.persistence.postgres.orm.annotations.Cardinality
import blogify.backend.persistence.postgres.orm.models.OrmTable
import blogify.backend.persistence.postgres.orm.models.PropertyMapping
import blogify.backend.resources.models.Resource

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ForeignKeyConstraint
import org.jetbrains.exposed.sql.IntegerColumnType
import org.jetbrains.exposed.sql.TextColumnType
import org.jetbrains.exposed.sql.UUIDColumnType

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

import com.andreapivetta.kolor.yellow
import com.andreapivetta.kolor.cyan
import com.andreapivetta.kolor.lightGreen
import com.andreapivetta.kolor.lightMagenta
import com.andreapivetta.kolor.lightRed

class ClassMapperTest {

    private data class TestClass (
        val name: String,
        val age: Int,
        @Invisible val password: String
    ) : Resource()

    @Test fun `should map simple class with only values properly`() {
        val tables = ClassMapper.mapClasses(TestClass::class, ComplexTestClass::class)

        tables.forEach { println(dumpOrmTable(it) + "\n") }

        val table = tables.toList().first()

        // Check PK

        assertNotNull(table.primaryKey)
        assertTrue(table.primaryKey!!.columns.singleOrNull { it.name == "uuid" } != null)

        // Check mappings

        assertEquals(1, table.mappings.count { it is PropertyMapping.IdentifierMapping })
        assertEquals(3, table.mappings.count { it is PropertyMapping.ValueMapping })

        // Check columns

        assertTrue(table.columns.any { it.name == "uuid" && it.columnType is UUIDColumnType })
        assertTrue(table.columns.any { it.name == "name" && it.columnType is TextColumnType })
        assertTrue(table.columns.any { it.name == "age" && it.columnType is IntegerColumnType  })
        assertTrue(table.columns.any { it.name == "password" && it.columnType is TextColumnType })
    }

    private data class ComplexTestClass (
        val name: String,
        val age: Int,
        val test: TestClass,
        @Invisible val password: String
    ) : Resource()

    @Test fun `should map complex class with values and associations properly`() {
        val table = ClassMapper.mapSingleClass(ComplexTestClass::class)

        println(dumpOrmTable(table) + "\n")

        // Check PK

        assertNotNull(table.primaryKey)
        assertTrue(table.primaryKey!!.columns.size == 1)
        assertTrue(table.primaryKey!!.columns.first().let { it.name == "uuid" && it.columnType is UUIDColumnType })

        // Check mappings

        assertEquals(1, table.mappings.count { it is PropertyMapping.IdentifierMapping })
        assertEquals(3, table.mappings.count { it is PropertyMapping.ValueMapping })
        assertEquals(1, table.mappings.count { it is PropertyMapping.AssociativeMapping<*> })

        // Check columns

        assertTrue(table.columns.any { it.name == "uuid" && it.columnType is UUIDColumnType })
        assertTrue(table.columns.any { it.name == "name" && it.columnType is TextColumnType })
        assertTrue(table.columns.any { it.name == "age" && it.columnType is IntegerColumnType  })
        assertFalse(table.columns.any { it.name == "test" })
        assertTrue(table.columns.any { it.name == "password" && it.columnType is TextColumnType })

        assertFalse(table.fullyMapped())

        // Check assoc. mapping

        val assocMapping = table.mappings.first { it is PropertyMapping.AssociativeMapping<*> } as PropertyMapping.AssociativeMapping<*>

        assertFalse(assocMapping.complete)
        assertTrue(assocMapping.cardinality == PropertyMapping.AssociativeMapping.Cardinality.ONE_TO_ONE)
        assertTrue(assocMapping.dependency == TestClass::class)
    }

    @Test fun `should resolve associative mappings properly`() {
        val tables = ClassMapper.mapClasses(TestClass::class, ComplexTestClass::class).toList()
        val testClassTable = tables[0]
        val complexClassTable = tables[1]

        tables.forEach { println(dumpOrmTable(it) + "\n") }

        ClassMapper.resolveAssociativeMappings(complexClassTable)
        assertEquals(0, complexClassTable.dependencyTables.size)

        @Suppress("UNCHECKED_CAST")
        val assocColumn = complexClassTable.mappings.first { it is PropertyMapping.AssociativeMapping<*> }
            .let { (it as PropertyMapping.AssociativeMapping<*>) to complexClassTable.columns.first { col -> col.name == "test" } }

        assertTrue(assocColumn.first.complete)

        // Check FK

        assertNotNull(assocColumn.second.foreignKey)

        val assocColumnFk = assocColumn.second.foreignKey!!

        assertEquals(assocColumn.second, assocColumnFk.from)
        assertEquals(testClassTable.identifyingColumn, assocColumnFk.target)

    }

    private data class TestClassCollections (
        val name: String,
        val age: Int,
        val tests: Set<@Cardinality(PropertyMapping.AssociativeMapping.CollectionCardinality.MANY_TO_MANY) TestClass>,
        @Invisible val password: String
    ) : Resource()

    @Test fun `should resolve associative mappings with collection properties properly`() {
        val tables = ClassMapper.mapClasses(TestClass::class, TestClassCollections::class).toList()

        tables.forEach { println(dumpOrmTable(it) + "\n") }

        val testClassTable = tables[0]
        val collectionClassTable = tables[1]

        // Check associative table

        assertEquals(1, collectionClassTable.dependencyTables.size)

        val assocTable = collectionClassTable.dependencyTables.first()

        // Check assoc. table PK

        assertNotNull(assocTable.primaryKey)
        assertEquals(2, assocTable.primaryKey!!.columns.size)

        assertTrue(assocTable.primaryKey!!.columns.any { it.name == "TestClassCollections" && it.columnType is UUIDColumnType })
        assertTrue(assocTable.primaryKey!!.columns.any { it.name == "TestClass" && it.columnType is UUIDColumnType })

        // Check assoc. table FKs

        assertEquals(2, assocTable.columns.count { it.foreignKey != null })
        val (fk0, fk1) = assocTable.columns.mapNotNull { it.foreignKey }

        assertTrue(fk0.from.table == assocTable)
        assertTrue(fk0.from.name == "TestClassCollections")
        assertTrue(fk0.target.table == collectionClassTable)
        assertTrue(fk0.target.name == "uuid")

        assertTrue(fk1.from.table == assocTable)
        assertTrue(fk1.from.name == "TestClass")
        assertTrue(fk1.target.table == testClassTable)
        assertTrue(fk1.target.name == "uuid")

        // Check assoc. table indices

        assertEquals(0, assocTable.indices.count { it.unique })
    }

    private fun dumpOrmTable(table: OrmTable<*>): String {
        val stream = StringBuilder()

        stream.append(dumpTable(table))
        stream.append("\n|  Dep. tables : " +
                if (table.dependencyTables.isNotEmpty())
                    table.dependencyTables.joinToString(prefix = "\n") { dumpTable(it).prependIndent("|\t\t") }
                else "<none>".yellow()
        )

        return stream.toString()
    }

    private fun dumpTable(table: Table): String {
        val stream = StringBuilder()

        fun dumpColumn(column: Column<*>): String =
            "${column.let { if (it.indexInPK != null) it.name.lightRed() else it.name.lightGreen() }}: ${column.columnType::class.simpleName?.cyan()}"

        fun dumpFk(forTable: Table, fk: ForeignKeyConstraint): String {
            val fromTableName = fk.from.table.tableName

            return "${if (fromTableName == forTable.tableName) "self".lightMagenta() else fromTableName.lightMagenta()}(${fk.from.name.lightGreen()})" +
                    " -> ${fk.target.table.tableName.lightMagenta()}(${fk.target.name.lightGreen()})"
        }

        stream.append("[Table ${table.tableName.lightMagenta()}]")
        stream.append("\n|  Columns : " + table.columns.sortedBy { if (it.indexInPK != null) 0 else 1 }.map { dumpColumn(it) })
        stream.append("\n|  FKs :     " +
                    table.columns
                        .mapNotNull { it.foreignKey }
                        .map { dumpFk(table, it) }
                        .ifEmpty { "[${"<none>".yellow()}]" }
        )
        return stream.toString()
    }

}
