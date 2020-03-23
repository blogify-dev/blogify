package blogify.backend.persistence.postgres.orm

import blogify.backend.annotations.Invisible
import blogify.backend.persistence.postgres.orm.models.OrmTable
import blogify.backend.persistence.postgres.orm.models.PropertyMapping
import blogify.backend.persistence.postgres.orm.models.PropertyMapping.AssociativeMapping.Cardinality as AssociationCardinality
import blogify.backend.resources.models.Resource
import blogify.backend.resources.reflect.models.ext.handle
import blogify.backend.resources.reflect.models.ext.uuidHandle

import org.jetbrains.exposed.sql.IntegerColumnType
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.TextColumnType

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

object PropertyMapperTest {

    private data class TestClass (
        val name: String,
        val age: Int,
        @Invisible val password: String
    ) : Resource()

    private data class ComplexTestClass (
        val name: String,
        val age: Int,
        val test: TestClass,
        @Invisible val password: String
    ) : Resource()

    @Test
    fun `should map value properties correctly`() {
        val uuidMapping = PropertyMapper.mapProperty(ComplexTestClass::class, ComplexTestClass::class.uuidHandle)

        val nameMapping = PropertyMapper.mapProperty(ComplexTestClass::class, ComplexTestClass::name.handle())
        assertTrue(nameMapping is PropertyMapping.ValueMapping)

        val ageMapping = PropertyMapper.mapProperty(ComplexTestClass::class, ComplexTestClass::age.handle())
        assertTrue(ageMapping is PropertyMapping.ValueMapping)

        val testTable = OrmTable(ComplexTestClass::class, setOf(uuidMapping, nameMapping, ageMapping))

        assertTrue(testTable.columns.any { it.name == "name" && it.columnType is TextColumnType })
        assertTrue(testTable.columns.any { it.name == "age" && it.columnType is IntegerColumnType })
    }

    @Test fun `should map associative properties properly`() {
        val testMapping = PropertyMapper.mapProperty(ComplexTestClass::class, ComplexTestClass::test.handle())

        assertTrue(testMapping is PropertyMapping.AssociativeMapping<*>)
        assertTrue((testMapping as PropertyMapping.AssociativeMapping<*>).cardinality == AssociationCardinality.ONE_TO_ONE)

        // TODO add column testing when it's ready
    }

}
