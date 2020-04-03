package blogify.backend.persistence.postgres.orm.query

import blogify.backend.persistence.postgres.orm.extensions.mappedTable
import blogify.backend.persistence.postgres.orm.extensions.mapping
import blogify.backend.persistence.postgres.orm.models.PropertyMapping
import blogify.backend.resources.models.Resource
import blogify.backend.testutils.SqlUtils

import org.jetbrains.exposed.sql.Column

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class QueryInterfaceFieldTest {

    private open class Person (
        val name: String,
        val age: Int
    ) : Resource()

    @Test fun `should get correct select fields for simple class`() {
        val people = Person::class.mappedTable

        val name = Person::name.mapping as PropertyMapping.ValueMapping
        val age = Person::age.mapping as PropertyMapping.ValueMapping

        val fields = people.queryInterface
                .findFieldsForMappings(listOf(name, age))

        println(fields.fields.map { SqlUtils.dumpExpression(it, verbose = true) })

        assertTrue(name.column in fields.fields)
        assertTrue(age.column in fields.fields)
    }

    private data class Car (
        val brand: String,
        val color: String,
        val modelYear: Int
    ) : Resource()

    private data class PersonWithCar (
        val name: String,
        val age: Int,
        val car: Car?
    ) : Resource()

    @Suppress("UNCHECKED_CAST")
    @Test fun `should get correct select fields for complex class`() {
        Car::class.mappedTable // Force mapping
        val marriedPeople = PersonWithCar::class.mappedTable

        val name = PersonWithCar::name.mapping as PropertyMapping.ValueMapping
        val age = PersonWithCar::age.mapping as PropertyMapping.ValueMapping
        val car = PersonWithCar::car.mapping as PropertyMapping.AssociativeMapping<PersonWithCar>
        val carColor = Car::color.mapping as PropertyMapping.ValueMapping

        val fields = marriedPeople.queryInterface
                .findFieldsForMappings(listOf(name, age, car, carColor))

        println(fields.fields.map { SqlUtils.dumpExpression(it, verbose = true) })

        assertEquals(3, fields.fields.size)

        assertTrue(name.column in fields.fields)
        assertTrue(age.column in fields.fields)
        assertTrue(carColor.column in fields.fields)
    }

    @Suppress("UNCHECKED_CAST")
    @Test fun `should get correct select fields with keys for complex class`() {
        val cars = Car::class.mappedTable // Force mapping
        val peopleWithCars = PersonWithCar::class.mappedTable

        val name = PersonWithCar::name.mapping as PropertyMapping.ValueMapping
        val age = PersonWithCar::age.mapping as PropertyMapping.ValueMapping
        val car = PersonWithCar::car.mapping as PropertyMapping.AssociativeMapping<PersonWithCar>
        val carColor = Car::color.mapping as PropertyMapping.ValueMapping

        val fields = peopleWithCars.queryInterface
                .findFieldsForMappings(listOf(name, age, car, carColor), keepImplicitKeys = true)

        println(fields.fields.map { SqlUtils.dumpExpression(it, verbose = true) })

        assertEquals(4, fields.fields.size)

        assertTrue(name.column in fields.fields)
        assertTrue(age.column in fields.fields)
        assertTrue(fields.fields.any { it is Column<*>
                && it.foreignKey != null
                && it.foreignKey!!.from.table == peopleWithCars
                && it.foreignKey!!.target.table == cars
        })
        assertTrue(carColor.column in fields.fields)
    }

}
