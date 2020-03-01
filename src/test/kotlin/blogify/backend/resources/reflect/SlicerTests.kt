package blogify.backend.resources.reflect

import blogify.backend.annotations.Invisible
import blogify.backend.notifications.models.NotificationTarget
import blogify.backend.resources.models.Resource
import blogify.backend.resources.reflect.models.ext.ok

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class SlicerTests {

    data class TestClass (
        val name: String,
        val age: Int,
        @Invisible val password: String
    ) : Resource() {
        override val targets: Set<NotificationTarget>
            get() = TODO("Not yet implemented")
    }

    val propMap = TestClass::class.cachedUnsafePropMap()
    val testObject = TestClass("abc", 17, "whatever")
    val slicedObject = testObject.slice(propMap.ok().keys - "uuid")

    @Test
    fun `should include visible properties in sliced resource`() {
        assertTrue(slicedObject["name"] == "abc" && slicedObject["age"] == 17, "Should contain valid properties")
    }

    @Test
    fun `should not include invisible properties in sliced resource`() {
        assertTrue(slicedObject["password"] == null, "Should not contain password in main values")
        assertTrue(slicedObject["_accessDenied"] == setOf("password"), "Should contain password in _accessDenied")
    }

}
