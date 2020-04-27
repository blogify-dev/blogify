package blogify.backend.resources.reflect

import annotations.Invisible
import annotations.check
import blogify.backend.events.models.EventTarget
import computed.models.Computed
import blogify.backend.resources.models.Resource
import reflect.models.extensions.ok
import reflect.models.extensions.valid

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import reflect.cachedPropMap

import kotlin.reflect.full.findAnnotation

class PropMapTests {

    data class TestClass(val visible: String, @Invisible val invisible: String): Resource() {
        override val targets: Set<EventTarget>
            get() = TODO("Not yet implemented")
    }

    @Test
    fun `valid() should not return Invisible properties`() {
        val none = TestClass::class.cachedPropMap().valid()
            .none { it.value.property.findAnnotation<Invisible>() !== null }

        assertTrue(none, "Should not contain @Invisible properties")
    }

    @Test
    fun `ok() should not return Invisible or Computed properties`() {
        val none = TestClass::class.cachedPropMap().ok()
            .none { it.value.property.findAnnotation<Invisible>() != null|| it.value.property.findAnnotation<Computed>() != null }

        assertTrue(none, "Should not contain @Invisible or @Computed properties")
    }

    data class TestClassWithRegexes(val noRegex: String, val withRegex: @check("[a-zA-Z0-9]{3}") String): Resource() {
        override val targets: Set<EventTarget>
            get() = TODO("Not yet implemented")
    }

    @Test
    fun `should pick up regexes`() {
        val prop = TestClassWithRegexes::class.cachedPropMap().ok().values.first { it.name == "withRegex" }

        val hasRegex = prop.regexCheck != null
        val regexPattern = prop.regexCheck?.pattern

        assertTrue(hasRegex, "Should have a non-null regex property")
        assertEquals("[a-zA-Z0-9]{3}", regexPattern)
    }

}
