package blogify.reflect.entity.instantiation

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

import java.lang.IllegalStateException

class PolymorphicTypeResolverTest {

    @Suppress("CanSealedSubClassBeObject")
    sealed class TestClass (
        val name: String
    ) {
        class A(name: String) : TestClass(name)

        class B(name: String, val age: Int) : TestClass(name)
    }

    @Test fun `should resolve simple sealed subtypes`() {
        val dto = mapOf (
            "name" to "John Doe",
            "age" to 17
        )

        val type = PolymorphicTypeResolver.resolveType(dto, TestClass::class)

        assertEquals(TestClass.B::class, type)
    }

    @Test fun `should throw exception on ambiguous type`() {
        val dto1 = mapOf (
            "name" to "Jane Smith"
        )

        assertThrows(IllegalStateException::class.java) {
            PolymorphicTypeResolver.resolveType(dto1, TestClass::class)
        }
    }

    @Test fun `should throw exception on no matching type`() {
        val dto1 = mapOf (
            "numberOfKids" to 5
        )

        assertThrows(IllegalStateException::class.java) {
            PolymorphicTypeResolver.resolveType(dto1, TestClass::class)
        }
    }

}
