package blogify.backend.resources.reflect

import blogify.backend.annotations.Invisible
import blogify.backend.events.models.EventTarget
import blogify.backend.resources.models.Resource
import blogify.backend.resources.reflect.models.ext.ok
import blogify.backend.util.Sr

import kotlinx.coroutines.runBlocking

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class InstnatiatorTests {

    data class TestClass (
        val name: String,
        val age: Int,
        @Invisible val password: String
    ) : Resource() {
        @Invisible override val targets: Set<EventTarget>
            get() = TODO("Not yet implemented")
    }

    val propMap = SlicerTests.TestClass::class.cachedUnsafePropMap()
    val testObject = TestClass("abc", 17, "whatever")
    val testData = mapOf (
        "name" to "abc",
        "age" to 17,
        "password" to "whatever"
    )

    @Test fun `instantiate should create object correctly`() {
        runBlocking {
            val propHandleDto = testData.mapKeys { propMap.ok()[it.key] ?: error("") }
            val newInstance = TestClass::class.doInstantiate(propHandleDto, externalFetcher = { _, _ -> Sr.of { error("") } })

            assertEquals(testObject, newInstance.get())
        }
    }

}
