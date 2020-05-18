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
        val oneOfFilter = Filter.IsOneOf(Test::x, setOf("hi !", "hello !"))
        val notOneOfFilter = Filter.IsNotOneOf(Test::x, setOf("goodbye !", "bye !"))

        assertTrue(filter matchesForValue "hi !")
        assertFalse(notFilter matchesForValue "hi !")
        assertTrue(oneOfFilter matchesForValue "hi !")
        assertTrue(oneOfFilter matchesForValue "hello !")
        assertFalse(oneOfFilter matchesForValue "howdy !")
        assertTrue(notOneOfFilter matchesForValue "hi !")
        assertFalse(notOneOfFilter matchesForValue "bye !")

        val test = Test("hi !")
        val test2 = Test("hello !")
        val test3 = Test("howdy !")
        val test4 = Test("goodbye !")

        assertTrue(filter matchesFor test)
        assertFalse(notFilter matchesFor test)
        assertTrue(oneOfFilter matchesFor test)
        assertTrue(oneOfFilter matchesFor test2)
        assertFalse(oneOfFilter matchesFor test3)
        assertTrue(notOneOfFilter matchesFor test2)
        assertFalse(notOneOfFilter matchesFor test4)
    }

    @Test fun `should not be able to create filter for invalid handle`() {
        class Test(@Hidden val x: String) : Mapped()

        assertThrows(AssertionError::class.java) {
            Filter.IsValue(Test::x, "hi !")
        }
    }

}
