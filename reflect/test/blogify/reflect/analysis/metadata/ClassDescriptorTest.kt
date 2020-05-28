package blogify.reflect.analysis.metadata

import blogify.reflect.analysis.extensions.descriptor
import blogify.reflect.models.Mapped

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertDoesNotThrow

class ClassDescriptorTest {

    private class A (
        val name: String,
        val age: Int
    ) : Mapped()

    @Test fun `should find class descriptor for simple class`() {
        assertDoesNotThrow {
            A::class.descriptor
        }
    }

    @Test fun `should have all property descriptors in simple class descriptor`() {
        val descriptor = A::class.descriptor

        arrayOf(A::name, A::age).forEach { property ->
            assertTrue(descriptor.propertyDescriptors.contains(property))
        }
    }

}
