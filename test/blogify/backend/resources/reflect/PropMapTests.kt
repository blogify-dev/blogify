package blogify.backend.resources.reflect

import blogify.backend.entity.Resource
import blogify.backend.events.models.EventTarget
import blogify.reflect.computed.models.Computed
import blogify.reflect.annotations.Hidden
import blogify.reflect.annotations.check
import blogify.reflect.models.extensions.ok
import blogify.reflect.models.extensions.valid
import blogify.reflect.propMap

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

import kotlin.reflect.full.findAnnotation

class PropMapTests {

    data class TestClass(val visible: String, @Hidden val invisible: String): Resource() {
        override val targets: Set<EventTarget>
            get() = TODO("Not yet implemented")
    }

    @Test
    fun `valid() should not return Invisible properties`() {
        val none = TestClass::class.propMap.valid
            .none { it.value.property.findAnnotation<Hidden>() !== null }

        assertTrue(none, "Should not contain @Invisible properties")
    }

    @Test
    fun `ok() should not return Invisible or Computed properties`() {
        val none = TestClass::class.propMap.ok
            .none { it.value.property.findAnnotation<Hidden>() != null|| it.value.property.findAnnotation<Computed>() != null }

        assertTrue(none, "Should not contain @Invisible or @Computed properties")
    }

    data class TestClassWithRegexes(val noRegex: String, val withRegex: @check("[a-zA-Z0-9]{3}") String): Resource() {
        override val targets: Set<EventTarget>
            get() = TODO("Not yet implemented")
    }

    @Test
    fun `should pick up regexes`() {
        val prop = TestClassWithRegexes::class.propMap.ok.values.first { it.name == "withRegex" }

        val hasRegex = prop.regexCheck != null
        val regexPattern = prop.regexCheck?.pattern

        assertTrue(hasRegex, "Should have a non-null regex property")
        assertEquals("[a-zA-Z0-9]{3}", regexPattern)
    }

}
