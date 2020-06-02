package blogify.reflect.entity.metadata

import blogify.reflect.analysis.extensions.descriptor
import blogify.reflect.annotations.Hidden
import blogify.reflect.models.Mapped

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class EntityMetadataTest {

    class A (
        @Hidden val name: String
    ) : Mapped()

    @Test fun `should find property with @Hidden annotation to not be visible`() {
        val descriptor = A::name.descriptor

        assertFalse(descriptor.entity.isVisible)
    }

}