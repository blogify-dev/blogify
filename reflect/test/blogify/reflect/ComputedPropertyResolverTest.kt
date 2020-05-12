package blogify.reflect

import blogify.reflect.computed.extensions.computed
import blogify.reflect.computed.models.ComputedPropContainer
import blogify.reflect.computed.resolveComputedProps
import blogify.reflect.models.Mapped

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

import java.lang.IllegalStateException

class ComputedPropertyResolverTest {

    @Test fun should_resolve_a_basic_computed_property() {
        val testObject = object : Mapped() {
            val name: String = ""
            val computed = computed { "ha !" }
        }

        val resolved = resolveComputedProps(testObject)

        assertEquals(resolved.entries.first().value.resolution, ComputedPropContainer.Resolution.Value("ha !"))
    }

    class CustomContainer<TMapped : Mapped> (
        override val obj: TMapped
    ) : ComputedPropContainer<TMapped, String>() {

        override var resolved: String? = null

    }

    @Test fun should_throw_an_error_given_custom_container_without_providing_a_custom_resolver() {
        val testObject = object : Mapped() {
            val name: String = ""
            val computed = CustomContainer<Mapped>(this)
        }

        assertThrows(IllegalStateException::class.java) {
            resolveComputedProps(testObject)
        }
    }

    @Test fun should_use_custom_resolver_to_handle_custom_container() {
        val testObject = object : Mapped() {
            val name: String = ""
            val computed = CustomContainer<Mapped>(this)
        }

        val resolved = resolveComputedProps(testObject) {
            when (it) {
                is CustomContainer -> "hello !"
                else -> null
            }
        }

        assertEquals(resolved.entries.first().value.resolution, ComputedPropContainer.Resolution.Value("hello !"))
    }

    @Test fun should_resolve_basic_computed_property() {
        val testObject = object : Mapped() {
            val name = computed { "Hello, World!" }
        }

        resolveComputedProps(testObject)

        val sliced = testObject.sanitize()

        assertEquals("Hello, World!", sliced["name"])
    }

}
