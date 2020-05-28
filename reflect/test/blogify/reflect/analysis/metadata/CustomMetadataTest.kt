package blogify.reflect.analysis.metadata

import blogify.reflect.analysis.extensions.descriptor
import blogify.reflect.analysis.models.PropertyDescriptor
import blogify.reflect.analysis.models.metadata.Metadata
import blogify.reflect.analysis.models.metadata.MetadataProvider
import blogify.reflect.models.Mapped

import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class CustomMetadataTest {

    @Target(AnnotationTarget.PROPERTY)
    @Retention
    private annotation class Important

    private class TestMetadata (
        val isImportant: Boolean
    ) : Metadata<KProperty<*>> {

        object Provider : MetadataProvider<TestMetadata, KProperty<*>> {
            override fun provideFor(element: KProperty<*>): TestMetadata =
                TestMetadata(element.findAnnotation<Important>() != null)
        }

    }

    private class A (
       @Important val name: String
    ) : Mapped()

    private val PropertyDescriptor.status: TestMetadata
        get() = this.getOrMake(TestMetadata.Provider)

    @Test fun `should be able to find test metadata on property`() {
        assertTrue(A::name.descriptor.status.isImportant)
    }

}
