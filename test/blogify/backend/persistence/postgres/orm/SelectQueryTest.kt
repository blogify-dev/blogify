package blogify.backend.persistence.postgres.orm

import blogify.backend.TestDatabse
import blogify.backend.annotations.Invisible
import blogify.backend.persistence.postgres.orm.models.OrmTable
import blogify.backend.persistence.postgres.orm.models.PropertyMapping
import blogify.backend.persistence.postgres.orm.query.extensions.expr
import blogify.backend.resources.models.Resource

import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.*

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SelectQueryTest {

    private lateinit var tables: Set<OrmTable<*>>

    private lateinit var testClassTable: OrmTable<TestClass>
    private lateinit var complexTestClassTable: OrmTable<ComplexTestClass>

    @Suppress("UNCHECKED_CAST")
    @BeforeAll fun setUpDatabaseAndTables() {
        TestDatabse.init()

        tables = ClassMapper.mapClasses(TestClass::class, ComplexTestClass::class)
        transaction { SchemaUtils.create(*tables.toTypedArray()) }

        tables.forEach { println(ClassMapperTest.dumpOrmTable(it) + "\n") }

        testClassTable = tables.toList()[0] as OrmTable<TestClass>
        complexTestClassTable = tables.toList()[1] as OrmTable<ComplexTestClass>
    }

    @BeforeEach fun clearTables() {
      transaction {
          complexTestClassTable.deleteAll()
          testClassTable.deleteAll()
      }
    }

    private data class TestClass (
        val name: String,
        val age: Int,
        @Invisible val password: String
    ) : Resource()

    @Test fun `can create select query on simple table properly`() {
        @Suppress("UNCHECKED_CAST")
        transaction {
            this.exec("insert into testclass (name, age, password, uuid) " +
                    "values ('haha', 16, '123', 'd5ab0852-fcd9-4fca-9674-7e685955f616');")

            this.exec("insert into testclass (name, age, password, uuid) " +
                    "values ('owo', 26, 'qwerty', '7fe56dc8-97b9-4c55-84a2-6b745c99b207');")
        }

        val nameMapping = testClassTable[TestClass::name] as PropertyMapping.ValueMapping
        val ageMapping = testClassTable[TestClass::age] as PropertyMapping.ValueMapping

        val fields = testClassTable.queryInterface
            .findFieldsForMappings(listOf(nameMapping, ageMapping))

        val results = transaction {
            Query (
                testClassTable.queryInterface.findFieldsForMappings(listOf(nameMapping, ageMapping)),
                Op.TRUE
            ).toList()
        }

        results.forEach { result ->
            println(result.fieldIndex.map { expr -> ClassMapperTest.dumpExpression(expr.key, true) + " = " + result[expr.key] +"\n" })
        }

        assertEquals(2, results.size)
        results.map { it.fieldIndex.keys }.forEach { exprs ->
            assertTrue(exprs.any { it is Column<*> && it.name == "name" && it.table == testClassTable })
            assertTrue(exprs.any { it is Column<*> && it.name == "age"  && it.table == testClassTable })
        }
    }

    private data class ComplexTestClass (
        val name: String,
        val age: Int,
        val test: TestClass,
        @Invisible val password: String
    ) : Resource()

    @Suppress("UNCHECKED_CAST")
    @Test fun `should create query on complex table properly`() {
        @Suppress("UNCHECKED_CAST")
        transaction {
            this.exec("insert into testclass (name, age, password, uuid) " +
                    "values ('haha', 16, '123', 'd5ab0852-fcd9-4fca-9674-7e685955f616');")

            this.exec("insert into testclass (name, age, password, uuid) " +
                    "values ('everyone', 23, 'xyz', 'dfec1b67-bf3e-4e5f-978e-dfaf09965709');")

            this.exec("insert into complextestclass (name, age, test, password, uuid) " +
                    "values ('hehe', 23, 'd5ab0852-fcd9-4fca-9674-7e685955f616', '12345', '3d506d63-4804-4e76-ac13-8d69dfb957d0');")

            this.exec("insert into complextestclass (name, age, test, password, uuid) " +
                    "values ('hello', 75, 'd5ab0852-fcd9-4fca-9674-7e685955f616', '575', '1660442a-1e86-4123-8048-03b9e3cf6292');")

            this.exec("insert into complextestclass (name, age, test, password, uuid) " +
                    "values ('woah hehe wow', 41, 'd5ab0852-fcd9-4fca-9674-7e685955f616', '12345', '788ff209-a524-4aac-b172-49385a25a818');")

            this.exec("insert into complextestclass (name, age, test, password, uuid) " +
                    "values ('hehe hello', 51, 'dfec1b67-bf3e-4e5f-978e-dfaf09965709', '87541', 'b6ac4364-810f-41ea-9e97-8acef0490789');")

            this.exec("insert into complextestclass (name, age, test, password, uuid) " +
                    "values ('hehe world', 11, 'dfec1b67-bf3e-4e5f-978e-dfaf09965709', '21032415', '78d2b39f-904c-4c02-a183-5b192039d13a');")
        }

        val nameMapping = complexTestClassTable[ComplexTestClass::name]
        val testMapping = complexTestClassTable[ComplexTestClass::test]
        val testAgeMapping = testClassTable[TestClass::age] as PropertyMapping.ValueMapping

        val results = transaction {
            Query (
                complexTestClassTable.queryInterface.findFieldsForMappings(listOf(nameMapping, testMapping, testAgeMapping)),
                expr { (testAgeMapping() greater 10) and (testAgeMapping() less 20) }
            ).toList()
        }

        results.forEach { result ->
            println(result.fieldIndex.map { expr -> ClassMapperTest.dumpExpression(expr.key, true) + " = " + result[expr.key] +"\n" })
        }

        assertEquals(3, results.size)
        results.map { it.fieldIndex.keys }.forEach { exprs ->
            assertTrue(exprs.any { it is Column<*> && it.name == "name" && it.table == complexTestClassTable })
            assertTrue(exprs.any { it is Column<*> && it.name == "age"  && it.table == testClassTable })
        }
        assertTrue(results.all { it[testAgeMapping.column as Column<Int>] in (10..20) })
    }

}
