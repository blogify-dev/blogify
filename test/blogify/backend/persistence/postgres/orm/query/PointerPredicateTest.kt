package blogify.backend.persistence.postgres.orm.query

import blogify.backend.TestDatabse
import blogify.backend.persistence.postgres.orm.extensions.mappedTable
import blogify.backend.persistence.postgres.orm.query.extensions.then
import blogify.backend.persistence.postgres.orm.query.models.*
import blogify.backend.persistence.postgres.orm.query.models.Op
import blogify.backend.resources.models.Resource

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.Assertions.assertEquals

import java.util.*
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PointerPredicateTest {

    @BeforeAll fun initDatabase() = TestDatabse.init()

    private data class PersonContainer (
        val person: Person
    ) : Resource()

    private data class Person (
        val name: String,
        val height: Int,
        val friendCount: Int,
        val distanceFromJupiter: Long
    ) : Resource()

    @Test fun `should create correct conditions for simple value pointers`() {
        Person::class.mappedTable
        PersonContainer::class.mappedTable

        val rootPtr = Pointer<PersonContainer, PersonContainer, Person>(null, PersonContainer::person)

        val name = rootPtr then Person::name
        val height = rootPtr then Person::height
        val friendCount = rootPtr then Person::friendCount
        val distanceFromJupiter = rootPtr then Person::distanceFromJupiter

        val nameOp = PointerPredicate(name, Op.Equals, QueryParameter("kira", TextColumnType()))
        val notNameOp = PointerPredicate(name, Op.NotEquals, QueryParameter("robert", TextColumnType()))
        val heightOp = PointerPredicate(height, Op.GreaterOrEquals, QueryParameter(150, IntegerColumnType()))
        val friendCountOp = PointerPredicate(friendCount, Op.Less, QueryParameter(19, IntegerColumnType()))
        val distanceFromJupiterOp = PointerPredicate(distanceFromJupiter, Op.Greater, QueryParameter(109_000_596_789_654_888, LongColumnType()))

        val nameOpString = transaction {
             nameOp.toExpr().toString()
        }

        println(nameOpString)
        assertTrue(Regex("joined_ptr_\\w+\\.\"name\" = 'kira'").containsMatchIn(nameOpString))

        val notNameOpString = transaction {
            notNameOp.toExpr().toString()
        }

        println(notNameOpString)
        assertTrue(Regex("joined_ptr_\\w+\\.\"name\" <> 'robert'").containsMatchIn(notNameOpString))

        val heightOpString = transaction {
            heightOp.toExpr().toString()
        }

        println(heightOpString)
        assertTrue(Regex("joined_ptr_\\w+\\.height >= 150").containsMatchIn(heightOpString))

        val friendCountOpString = transaction {
            friendCountOp.toExpr().toString()
        }

        println(friendCountOpString)
        assertTrue(Regex("joined_ptr_\\w+\\.\"friendCount\" < 19").containsMatchIn(friendCountOpString))

        val distanceFromJupiterOpString = transaction {
            distanceFromJupiterOp.toExpr().toString()
        }

        println(distanceFromJupiterOpString)
        assertTrue(Regex("joined_ptr_\\w+\\.\"distanceFromJupiter\" > 109000596789654888").containsMatchIn(distanceFromJupiterOpString))
    }

    private data class Box (
        val thing: Thing
    ) : Resource()

    private data class Thing (
        val description: String
    ) : Resource()

    @Test fun `should create correct conditions for associative pointers`() {
        Thing::class.mappedTable
        Box::class.mappedTable

        val uuid = UUID.randomUUID()
        val notUuid = UUID.randomUUID()

        val thing = Pointer<Box, Box, Thing>(null, Box::thing)

        val thingOp = ResourcePointerPredicate(thing, ResourceOp.Equals, QueryParameter(uuid, UUIDColumnType()))

        val thingOpString = transaction {
            thingOp.toExpr().toString()
        }

        println(thingOpString)
        assertEquals("box.thing = '$uuid'", thingOpString)

        val notThingOp = ResourcePointerPredicate(thing, ResourceOp.NotEquals, QueryParameter(notUuid, UUIDColumnType()))

        val notThingOpString = transaction {
            notThingOp.toExpr().toString()
        }

        println(notThingOpString)
        assertEquals("box.thing <> '$notUuid'", notThingOpString)
    }

}
