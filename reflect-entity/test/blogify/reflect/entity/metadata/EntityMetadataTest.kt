package blogify.reflect.entity.metadata

import blogify.reflect.analysis.extensions.descriptor
import blogify.reflect.annotations.Hidden
import blogify.reflect.entity.annotations.NotUpdatable
import blogify.reflect.models.Mapped

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class EntityMetadataTest {

    class A (
        @Hidden val name: String,
        val age: Int,
        @NotUpdatable val truth: String
    ) : Mapped()

    @Test fun `should find property with @Hidden annotation to not be visible`() {
        val descriptor = A::name.descriptor

        assertFalse(descriptor.entity.isVisible)
    }

    @Test fun `should find property with @Hidden annotation to not be updatable`() {
        val descriptor = A::name.descriptor

        assertFalse(descriptor.entity.isUpdatable)
    }

    @Test fun `should find property with @NotUpdatable annotation to not be updatable`() {
        val descriptor = A::truth.descriptor

        assertFalse(descriptor.entity.isUpdatable)
    }

    @Test fun `should find property without @Hidden or @NotUpdatable annotation to be updatable`() {
        val descriptor = A::age.descriptor

        assertTrue(descriptor.entity.isUpdatable)
    }

}
