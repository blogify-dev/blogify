package blogify.backend.util

import blogify.common.util.MapCache

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

internal class MapCacheTest {

    @Test
    fun `should memorize data`() {
        val testSubject = MapCache<Int, String>()

        testSubject[5] = "hello"

        var didntCreateNeValue = true
        testSubject.findOr(5) { "Hello".also { didntCreateNeValue = false } }
        val cached = testSubject[5]

        assertEquals("hello", cached, "should have the right cached value for the key")
        assertTrue(didntCreateNeValue, "should not have created a new value for cached data")
    }

    @Test
    fun `should flush data properly`() {
        val testSubject = MapCache<Int, String>()

        testSubject[3] = "a"
        testSubject[5] = "b"
        testSubject[6] = "c"

        var didntCreateNeValue = true
        assertEquals("a", testSubject.findOr(3) { "a".also { didntCreateNeValue = false } }.get(),
            "should have the right cached value for the key")
        assertTrue(didntCreateNeValue, "should posses cached value")

        testSubject.flush()

        var createdNewValue = false
        assertEquals("b", testSubject.findOr(3) { "b".also { createdNewValue = true } }.get(),
            "should have the right cached value for the key")
        assertTrue(createdNewValue, "should not posses value after flush")
    }

}
