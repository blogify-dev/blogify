package blogify.filtering.metadata

import blogify.reflect.models.Mapped
import blogify.reflect.analysis.extensions.descriptor

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class FilteringMetadataTest {

    @Suppress("SelfReferenceConstructorParameter")
    class A (
        val name: String,
        @NotFilterable val someInternalValue: Int,
        val someComplexValue: A
    ) : Mapped()

    @Test fun `should accept primitive property as filterable property`() {
        val descriptor = A::name.descriptor

        assertTrue(descriptor.filtering.isFilterable)
    }

    @Test fun `should not accept primitive property with @NotFilterable as filterable property`() {
        val descriptor = A::someInternalValue.descriptor

        assertFalse(descriptor.filtering.isFilterable)
    }

    @Test fun `should not accept non-primitive property as filterable property`() {
        val descriptor = A::someComplexValue.descriptor

        assertFalse(descriptor.filtering.isFilterable)
    }

}
