package blogify.backend.resources.reflect

import blogify.backend.annotations.Invisible
import blogify.backend.events.models.EventTarget
import blogify.backend.resources.models.Resource
import blogify.backend.resources.reflect.models.extensions.ok
import blogify.backend.util.Sr

import kotlinx.coroutines.runBlocking

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

@Suppress("MapGetWithNotNullAssertionOperator")
class UpdaterTests {

    data class TestClass (
        val name: String,
        val age: Int,
        @Invisible val password: String
    ) : Resource() {
        @Invisible
        override val targets: Set<EventTarget>
            get() = setOf()
    }

    val propMap = TestClass::class.cachedUnsafePropMap()
    val testObject = TestClass("abc", 17, "whatever")
    val testUpdateData = mapOf (
        propMap.ok()["name"]!!     to "steven",
        propMap.ok()["age"]!!      to 18,
        propMap.ok()["password"]!! to "ha"
    )

    @Test fun `should update mapped object`() {
        runBlocking {
            val updated = testObject.update(testUpdateData) { _, _ -> Sr.of { error("") } }

            assertEquals("steven", updated.get().name)
            assertEquals(18, updated.get().age)
            assertEquals("ha", updated.get().password)
        }
    }

    val testObjectUnchanged = TestClass("def", 21, "dontlook")
    val testUpdateDataUnchanged = mapOf (
        propMap.ok()["name"]!!     to "wow",
        propMap.ok()["age"]!!      to 36,
        propMap.ok()["password"]!! to "ho"
    )

    @Test fun `should keep unchanged values`() {
        runBlocking {
            val updated = testObjectUnchanged.update(testUpdateDataUnchanged) { _, _ -> Sr.of { error("") } }

            assertEquals("wow", updated.get().name)
            assertEquals(36, updated.get().age)
            assertEquals("ho", updated.get().password)
        }
    }

}
