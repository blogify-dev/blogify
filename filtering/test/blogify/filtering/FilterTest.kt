package blogify.filtering

import blogify.reflect.models.Mapped

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class FilterTest {

    @Test fun `should work with simple filter`() {
        class Test(val x: String) : Mapped()

        val filter = Filter.IsValue(Test::x, "hi !")
        val notFilter = Filter.IsNotValue(Test::x, "hello !")

        assertTrue(filter matchesFor "hi !")
        assertTrue(notFilter matchesFor "hi !")
    }

}
