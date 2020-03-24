package blogify.backend.persistence.postgres.orm

import blogify.backend.annotations.Invisible
import blogify.backend.persistence.postgres.orm.annotations.Cardinality
import blogify.backend.persistence.postgres.orm.models.OrmTable
import blogify.backend.persistence.postgres.orm.models.PropertyMapping
import blogify.backend.resources.models.Resource

import org.jetbrains.exposed.sql.ForeignKeyConstraint
import org.jetbrains.exposed.sql.IntegerColumnType
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.TextColumnType
import org.jetbrains.exposed.sql.UUIDColumnType

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ClassMapperTest {

    private data class TestClass (
        val name: String,
        val age: Int,
        @Invisible val password: String
    ) : Resource()

    @Test fun `should map simple class with only values properly`() {
        val tables = ClassMapper.mapClasses(TestClass::class, ComplexTestClass::class)

        tables.forEach { println(dumpOrmTable(it)) }

        val table = tables.toList().first()

        assertEquals(1, table.mappings.count { it is PropertyMapping.IdentifierMapping })
        assertEquals(3, table.mappings.count { it is PropertyMapping.ValueMapping })

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

        println(dumpOrmTable(table))

        assertEquals(1, table.mappings.count { it is PropertyMapping.IdentifierMapping })
        assertEquals(3, table.mappings.count { it is PropertyMapping.ValueMapping })
        assertEquals(1, table.mappings.count { it is PropertyMapping.AssociativeMapping<*> })

        assertTrue(table.columns.any { it.name == "uuid" && it.columnType is UUIDColumnType })
        assertTrue(table.columns.any { it.name == "name" && it.columnType is TextColumnType })
        assertTrue(table.columns.any { it.name == "age" && it.columnType is IntegerColumnType  })
        assertFalse(table.columns.any { it.name == "test" })
        assertTrue(table.columns.any { it.name == "password" && it.columnType is TextColumnType })

        assertFalse(table.fullyMapped())

        val assocMapping = table.mappings.first { it is PropertyMapping.AssociativeMapping<*> } as PropertyMapping.AssociativeMapping<*>

        assertFalse(assocMapping.complete)
        assertTrue(assocMapping.cardinality == PropertyMapping.AssociativeMapping.Cardinality.ONE_TO_ONE)
        assertTrue(assocMapping.dependency == TestClass::class)
    }

    @Test fun `should resolve associative mappings properly`() {
        val tables = ClassMapper.mapClasses(TestClass::class, ComplexTestClass::class).toList()
        val testClassTable = tables[0]
        val complexClassTable = tables[1]

        tables.forEach { println(dumpOrmTable(it)) }

        ClassMapper.resolveAssociativeMappings(complexClassTable)
        assertEquals(0, complexClassTable.dependencyTables.size)

        @Suppress("UNCHECKED_CAST")
        val assocColumn = complexClassTable.mappings.first { it is PropertyMapping.AssociativeMapping<*> }
            .let { (it as PropertyMapping.AssociativeMapping<*>) to complexClassTable.columns.first { col -> col.name == "test" } }

        assertTrue(assocColumn.first.complete)

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
        val table = ClassMapper.mapClasses(TestClass::class, TestClassCollections::class).toList()[1]

        println(dumpOrmTable(table))
    }

    private fun dumpOrmTable(table: OrmTable<*>): String {
        val stream = StringBuilder()

        stream.append(dumpTable(table))
        stream.append("\n|  Dependency tables : " + table.dependencyTables.joinToString(prefix = "\n") { dumpTable(it).prependIndent("|\t\t") })

        return stream.toString()
    }

    private fun dumpTable(table: Table): String {
        val stream = StringBuilder()

        fun dumpFk(forTable: Table, fk: ForeignKeyConstraint): String {
            val fromTableName = fk.from.table.tableName

            return "${if (fromTableName == forTable.tableName) "self" else fromTableName}(${fk.from.name}) -> ${fk.target.table.tableName}(${fk.target.name})"
        }

        stream.append("[Table \"${table.tableName}\"]")
        stream.append("\n|  Columns : " + table.columns.map { "${it.columnType::class.simpleName} - ${it.name}"})
        stream.append("\n|  FKs : " + table.columns.filter { it.foreignKey != null }
            .map { dumpFk(table, it.foreignKey!!) })

        return stream.toString()
    }

}
