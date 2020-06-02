package blogify.reflect.entity.slicer

import blogify.reflect.models.Mapped
import blogify.reflect.annotations.Hidden

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class SlicerTest {

    class A (
        val name: String,
        val age: Int,
        @Hidden val deepestSecret: String
    ) : Mapped()

    private val a = A("James", 17, "killed his ex wife")

    @Test fun `should find simple properties on slice of object`() {
        assertEquals("James", a.slice()["name"])
        assertEquals(17, a.slice()["age"])
    }

    @Test fun `should not find properties annotated with @Hidden on object`() {
        assertNull(a.slice()["deepestSecret"])
    }

    @Test fun `should find properties annotated with @Hidden on object when using unsafe`() {
        assertEquals("James", a.slice(unsafe = true)["name"])
        assertNull(a.slice(unsafe = true)["deepestSecret"])
    }

    @Test fun `should not find properties not included in slice on object`() {
        val props = arrayOf(A::name)

        assertNull(a.slice(props)["age"])
    }

}
