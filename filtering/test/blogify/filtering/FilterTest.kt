package blogify.filtering

import blogify.reflect.annotations.Hidden
import blogify.reflect.models.Mapped

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

import java.lang.AssertionError

class FilterTest {

    @Test fun `should work with simple filters`() {
        class Test(val x: String) : Mapped()

        val filter = Filter.IsValue(Test::x, "hi !")
        val notFilter = Filter.IsNotValue(Test::x, "hi !")

        assertTrue(filter matchesFor "hi !")
        assertFalse(notFilter matchesFor "hi !")

        val test = Test("hi !")

        assertTrue(filter matchesFor test)
        assertFalse(notFilter matchesFor test)
    }

    @Test fun `should not be able to create filter for invalid handle`() {
        class Test(@Hidden val x: String) : Mapped()

        assertThrows(AssertionError::class.java) {
            Filter.IsValue(Test::x, "hi !")
        }
    }

}
