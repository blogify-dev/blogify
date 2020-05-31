package blogify.backend.resources.reflect.metadata

import blogify.reflect.analysis.extensions.descriptor
import blogify.reflect.annotations.Hidden
import blogify.reflect.models.Mapped

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class EntityMetadataTest {

    class A (
        @Hidden val name: String
    ) : Mapped()

    @Test fun `should find @Hidden annotation on EntityMetadata on property`() {
        val descriptor = A::name.descriptor

        assertTrue(descriptor.entity.hidden)
    }

}
