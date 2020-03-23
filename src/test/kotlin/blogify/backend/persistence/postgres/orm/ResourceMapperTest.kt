package blogify.backend.persistence.postgres.orm

import blogify.backend.annotations.Invisible
import blogify.backend.resources.models.Resource
import org.jetbrains.exposed.sql.IntegerColumnType
import org.jetbrains.exposed.sql.TextColumnType

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ResourceMapperTest {

    private data class TestClass (
        val name: String,
        val age: Int,
        @Invisible val password: String
    ) : Resource()

    @Test fun `should map simple class with only values properly`() {
        val table = ClassMapper.mapClasses(TestClass::class).first()

        println(table.columns.map { "${it.columnType::class.simpleName} - ${it.name}"})
    }

    private data class ComplexTestClass (
        val name: String,
        val age: Int,
        val test: TestClass,
        @Invisible val password: String
    ) : Resource()

    @Test fun `should map complex class with values and associations properly`() {
        val table = ClassMapper.mapClasses(ComplexTestClass::class).first()

        println(table.columns.map { "${it.columnType::class.simpleName} - ${it.name}"})

        assertNotNull(table)
        assertTrue(table.columns.any { it.name == "name" && it.columnType is TextColumnType })
        assertTrue(table.columns.any { it.name == "age" && it.columnType is IntegerColumnType  })
        assertTrue(table.columns.any { it.name == "password" && it.columnType is TextColumnType })
    }

}
