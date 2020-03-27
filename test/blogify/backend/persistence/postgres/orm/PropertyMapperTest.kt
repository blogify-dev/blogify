package blogify.backend.persistence.postgres.orm

import blogify.backend.annotations.Invisible
import blogify.backend.persistence.postgres.orm.models.Cardinality
import blogify.backend.persistence.postgres.orm.models.OrmTable
import blogify.backend.persistence.postgres.orm.models.PropertyMapping
import blogify.backend.resources.models.Resource
import blogify.backend.resources.reflect.models.ext.handle
import blogify.backend.resources.reflect.models.ext.uuidHandle

import org.jetbrains.exposed.sql.IntegerColumnType
import org.jetbrains.exposed.sql.TextColumnType

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

import com.andreapivetta.kolor.red

class PropertyMapperTest {

    private data class TestClass (
        val name: String,
        val age: Int,
        val spouse: String?,
        @Invisible val password: String
    ) : Resource()

    private data class ComplexTestClass (
        val name: String,
        val age: Int,
        val test: TestClass,
        val testStrings: Set<String>,
        @Invisible val password: String
    ) : Resource()

    @Test fun `should map value properties correctly`() {
        val uuidMapping = PropertyMapper.mapProperty(ComplexTestClass::class.uuidHandle)

        val nameMapping = PropertyMapper.mapProperty(ComplexTestClass::name.handle())
        assertTrue(nameMapping is PropertyMapping.ValueMapping)

        val ageMapping = PropertyMapper.mapProperty(ComplexTestClass::age.handle())
        assertTrue(ageMapping is PropertyMapping.ValueMapping)

        val testTable = OrmTable(ComplexTestClass::class, setOf(uuidMapping, nameMapping, ageMapping))

        assertTrue(testTable.columns.any { it.name == "name" && it.columnType is TextColumnType && !it.columnType.nullable })
        assertTrue(testTable.columns.any { it.name == "age" && it.columnType is IntegerColumnType && !it.columnType.nullable })
    }

    @Test fun `should map nullable value properties correctly`() {
        val spouseMapping = PropertyMapper.mapProperty(TestClass::spouse.handle())
        assertTrue(spouseMapping is PropertyMapping.ValueMapping)

        val testTable = OrmTable(ComplexTestClass::class, setOf(spouseMapping))

        assertTrue(testTable.columns.any { it.name == "spouse" && it.columnType.nullable })
    }

    private data class TestClassInvalidCollections (
        val name: String,
        val age: Int,
        val tests: Set<String>?,
        @Invisible val password: String
    ) : Resource()

    @Test fun `should throw an exception when mapping a nullable Collection property`() {
        assertThrows(IllegalArgumentException::class.java, {
            ClassMapper.mapClasses(TestClassInvalidCollections::class)
        }, "fatal: collection property types cannot be marked nullable".red())
    }

    @Test fun `should map associative properties properly`() {
        val testMapping = PropertyMapper.mapProperty(ComplexTestClass::test.handle())

        assertTrue(testMapping is PropertyMapping.AssociativeMapping<*>)
        assertTrue((testMapping as PropertyMapping.AssociativeMapping<*>).cardinality == Cardinality.ONE_TO_ONE)
    }

    @Test fun `should map primitive associative properties properly`() {
        val testMapping = PropertyMapper.mapProperty(ComplexTestClass::testStrings.handle())

        assertTrue(testMapping is PropertyMapping.PrimitiveAssociativeMapping<*>)
    }

}
