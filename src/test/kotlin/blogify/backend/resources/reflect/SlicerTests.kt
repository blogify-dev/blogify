package blogify.backend.resources.reflect

import blogify.backend.annotations.Invisible
import blogify.backend.events.models.EventTarget
import blogify.backend.resources.computed.models.Computed
import blogify.backend.resources.models.Resource
import blogify.backend.resources.reflect.models.Mapped
import blogify.backend.resources.reflect.models.ext.valid

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class SlicerTests {

    data class TestClass (
        val name: String,
        val age: Int,
        @Invisible val password: String
    ) : Resource() {
        @Invisible override val targets: Set<EventTarget>
            get() = TODO("Not yet implemented")
    }

    val propMap = TestClass::class.cachedPropMap()
    val testObject = TestClass("abc", 17, "whatever")
    val slicedObject = testObject.slice(propMap.map.keys - "uuid")
    val sanitizedObject = testObject.sanitize()

    @Test fun `slice should include visible properties`() {
        assertTrue(slicedObject["name"] == "abc" && slicedObject["age"] == 17, "Should contain valid properties")
    }

    @Test fun `slice should not include invisible properties`() {
        assertTrue(slicedObject["password"] == null, "Should not contain password in main values")
        assertTrue(slicedObject["_accessDenied"] == setOf("password", "targets"), "Should contain invisible properties in _accessDenied")
    }

    @Test fun `sanitize should include all visible properties`() {
        assertTrue(sanitizedObject.filterNot { it.key.startsWith('_') }.all { it.key in propMap.valid().keys }, "All keys of DTO should be in propMap.valid()")
    }

    data class MappedTestClass (
        val name: String,
        val age: Int,
        @Invisible val password: String
    ) : Mapped()

    val mappedPropMap = MappedTestClass::class.cachedPropMap()
    val mappedTestObject = MappedTestClass("abc", 17, "whatever")
    val mappedSlicedObject = mappedTestObject.slice(mappedPropMap.map.keys)

    @Test fun `should slice mapped object properly`() {
        assertTrue(mappedSlicedObject["name"] == "abc" && mappedSlicedObject["age"] == 17, "Should contain valid properties")
    }

    @Test fun `should not include invisible properties in sliced mapped object`() {
        assertTrue(mappedSlicedObject["password"] == null, "Should not contain password in main values")
        assertTrue(mappedSlicedObject["_accessDenied"] == setOf("password"), "Should contain invisible properties in _accessDenied")
    }

    data class BadMappedTestClass (
        val name: String,
        val age: Int,
        @Computed val password: String
    ) : Mapped()

    @Test fun `should throw an error when mapping a mapped class with a @Computed property`() {
        val exception = assertThrows<IllegalStateException>("Should throw an error when mapping the class") {
            BadMappedTestClass::class.cachedPropMap()
        }

        assertEquals("@Computed property can only appear on classes extending Resource", exception.message)
    }

}