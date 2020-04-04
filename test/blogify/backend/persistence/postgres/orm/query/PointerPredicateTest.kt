package blogify.backend.persistence.postgres.orm.query

import blogify.backend.TestDatabse
import blogify.backend.persistence.postgres.orm.extensions.mappedTable
import blogify.backend.persistence.postgres.orm.query.extensions.then
import blogify.backend.persistence.postgres.orm.query.models.Op
import blogify.backend.persistence.postgres.orm.query.models.Pointer
import blogify.backend.persistence.postgres.orm.query.models.PointerPredicate
import blogify.backend.resources.models.Resource

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.Assertions.assertEquals

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

    @Test fun `should create correct predicates for simple value pointers`() {
        Person::class.mappedTable
        PersonContainer::class.mappedTable

        val rootPtr = Pointer<PersonContainer, PersonContainer, Person>(null, PersonContainer::person)

        val name = rootPtr then Person::name
        val height = rootPtr then Person::height
        val friendCount = rootPtr then Person::friendCount
        val distanceFromJupiter = rootPtr then Person::distanceFromJupiter

        val nameOp = PointerPredicate(name, Op.Equals, QueryParameter("kira", TextColumnType()))
        val heightOp = PointerPredicate(height, Op.GreaterOrEquals, QueryParameter(150, IntegerColumnType()))
        val friendCountOp = PointerPredicate(friendCount, Op.Less, QueryParameter(19, IntegerColumnType()))
        val distanceFromJupiterOp = PointerPredicate(distanceFromJupiter, Op.Greater, QueryParameter(109_000_596_789_654_888, LongColumnType()))

        val nameOpString = transaction {
             nameOp.toExpr().toString()
        }

        assertEquals("person.\"name\" = 'kira'", nameOpString)

        val heightOpString = transaction {
            heightOp.toExpr().toString()
        }

        assertEquals("person.height >= 150", heightOpString)

        val friendCountOpString = transaction {
            friendCountOp.toExpr().toString()
        }

        assertEquals("person.\"friendCount\" < 19", friendCountOpString)

        val distanceFromJupiterOpString = transaction {
            distanceFromJupiterOp.toExpr().toString()
        }

        assertEquals("person.\"distanceFromJupiter\" > 109000596789654888", distanceFromJupiterOpString)
    }

}
