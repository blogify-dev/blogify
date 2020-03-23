package blogify.backend.persistence.postgres.orm

import blogify.backend.annotations.Invisible
import blogify.backend.persistence.postgres.orm.models.PropertyMapping
import blogify.backend.resources.Article
import blogify.backend.resources.Comment
import blogify.backend.resources.models.Resource
import org.jetbrains.exposed.sql.Column

import org.jetbrains.exposed.sql.IntegerColumnType
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
        val table = ClassMapper.mapClasses(TestClass::class, ComplexTestClass::class).first()

        println(table.columns.map { "${it.columnType::class.simpleName} - ${it.name}"})

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

        println(table.columns.map { "${it.columnType::class.simpleName} - ${it.name}"})

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

        println("Columns : " + complexClassTable.columns.map { "${it.columnType::class.simpleName} - ${it.name}"})
        println("FKs : " + complexClassTable.columns.filter { it.foreignKey != null }
            .map { it.foreignKey?.let { fk -> "${fk.from.table.tableName}[${fk.from.name}] -> ${fk.target.table.tableName}[${fk.target.name}]" } })
    }

}
