package blogify.backend.persistence.postgres.orm

import blogify.backend.annotations.Invisible
import blogify.backend.resources.models.Resource

import org.junit.jupiter.api.Test

class ResourceMapperTest {

    data class TestClass (
        val name: String,
        val age: Int,
        @Invisible val password: String
    ) : Resource()

    @Test fun `should map simple class with only values properly`() {
        val table = ResourceMapper.mapResourceClass(TestClass::class)

        println(table.columns.map { "${it.columnType::class.simpleName} - ${it.name}"})
    }

    data class ComplexTestClass (
        val name: String,
        val age: Int,
        val test: TestClass,
        @Invisible val password: String
    ) : Resource()

    @Test fun `should map complex class with values and associations properly`() {
        val table = ResourceMapper.mapResourceClass(ComplexTestClass::class)

        println(table.columns.map { "${it.columnType::class.simpleName} - ${it.name}"})
    }

}
