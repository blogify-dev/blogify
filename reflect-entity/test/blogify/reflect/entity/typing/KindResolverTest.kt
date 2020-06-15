package blogify.reflect.entity.typing

import blogify.reflect.analysis.extensions.descriptor
import blogify.reflect.entity.Entity
import blogify.reflect.entity.metadata.entity

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

import java.lang.IllegalStateException

class KindResolverTest {

    class A (
        val name: String,
        val length: Float
    ) : Entity()

    class B (
        val a: A,
        val arrayOfAs: Array<A>,
        val listOfAs: List<A>,
        val setOfAs: Set<A>
    ) : Entity()

    class C (
        val x: StringBuffer
    ) : Entity()

    @Test fun `should find string on A - name`() {
        assertEquals(Kind(Kind.Type.String, false), A::name.descriptor.entity.kind)
    }

    @Test fun `should find number on A - length`() {
        assertEquals(Kind(Kind.Type.Number, false), A::length.descriptor.entity.kind)
    }

    @Test fun `should find entity on B - a`() {
        assertEquals(Kind(Kind.Type.Entity, false), B::a.descriptor.entity.kind)
    }

    @Test fun `should find array of entity on B - arrayOfAs, B - listOfAs, B - setOAs`() {
        assertEquals(Kind(Kind.Type.Entity, true), B::arrayOfAs.descriptor.entity.kind)
        assertEquals(Kind(Kind.Type.Entity, true), B::listOfAs.descriptor.entity.kind)
        assertEquals(Kind(Kind.Type.Entity, true), B::setOfAs.descriptor.entity.kind)
    }

    @Test fun `should throw an exception on C - x`() {
        assertThrows(IllegalStateException::class.java) {
            C::x.descriptor.entity.kind
        }.also { it.printStackTrace() }
    }

}
