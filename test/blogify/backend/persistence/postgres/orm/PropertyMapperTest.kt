package blogify.backend.persistence.postgres.orm

import blogify.backend.annotations.Invisible
import blogify.backend.persistence.postgres.orm.models.Cardinality
import blogify.backend.persistence.postgres.orm.models.PropertyMapping
import blogify.backend.resources.models.Resource
import blogify.backend.resources.reflect.models.ext.okHandle
import blogify.backend.resources.reflect.models.ext.uuidHandle

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
        val testStrings: Set<String>,
        @Invisible val password: String
    ) : Resource()

    @Test fun `should map value properties correctly`() {
        val uuidMapping = PropertyMapper.mapProperty(ComplexTestClass::class.uuidHandle)
        assertTrue(uuidMapping is PropertyMapping.IdentifierMapping)

        val nameMapping = PropertyMapper.mapProperty(ComplexTestClass::name.okHandle())
        assertTrue(nameMapping is PropertyMapping.ValueMapping)

        val ageMapping = PropertyMapper.mapProperty(ComplexTestClass::age.okHandle())
        assertTrue(ageMapping is PropertyMapping.ValueMapping)
    }

    @Test fun `should map associative properties properly`() {
        val testMapping = PropertyMapper.mapProperty(ComplexTestClass::test.okHandle())

        assertTrue(testMapping is PropertyMapping.AssociativeMapping<*>)
        assertTrue((testMapping as PropertyMapping.AssociativeMapping<*>).cardinality == Cardinality.ONE_TO_ONE)
    }

    @Test fun `should map primitive associative properties properly`() {
        val testMapping = PropertyMapper.mapProperty(ComplexTestClass::testStrings.okHandle())

        assertTrue(testMapping is PropertyMapping.PrimitiveAssociativeMapping<*>)
    }

}
