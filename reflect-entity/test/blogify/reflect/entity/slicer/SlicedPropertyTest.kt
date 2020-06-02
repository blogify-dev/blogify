package blogify.reflect.entity.slicer

import blogify.reflect.annotations.Hidden
import blogify.reflect.models.Mapped

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertTrue

class SlicedPropertyTest {

    class A (
        val name: String,
        val age: Int?,
        @Hidden val deepestSecret: String
    ) : Mapped()

    private val a =
        A("James", 17, "killed his ex wife")

    @Test fun `should find Value on non-hidden property`() {
        val slice = A::name on a

        assertTrue(slice is SlicedProperty.Value)
    }

    @Test fun `should find NullableValue on non-hidden nullable property`() {
        val slice = A::age onNullable a

        assertTrue(slice is SlicedProperty.NullableValue)
    }

    @Test fun `should find NotFound on hidden property`() {
        val slice = A::deepestSecret on a

        assertTrue(slice is SlicedProperty.NotFound)
    }

}
