package blogify.backend.persistence.postgres.orm

import blogify.backend.annotations.Invisible
import blogify.backend.persistence.postgres.orm.models.Cardinality
import blogify.backend.persistence.postgres.orm.models.CollectionCardinality
import blogify.backend.persistence.postgres.orm.annotations.Cardinality as CardinalityAnnotation
import blogify.backend.persistence.postgres.orm.models.PropertyMapping
import blogify.backend.resources.models.Resource
import blogify.backend.testutils.SqlUtils

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

import org.jetbrains.exposed.sql.*

class ClassMapperTest {

    private data class TestClass (
        val name: String,
        val age: Int,
        @Invisible val password: String?
    ) : Resource()

    @Test fun `should map simple class with only values properly`() {
        val tables = ClassMapper.mapClasses(TestClass::class, ComplexTestClass::class)

        tables.forEach { println(SqlUtils.dumpOrmTable(it) + "\n") }

        val table = tables.toList().first()

        // Check PK

        assertNotNull(table.primaryKey)
        assertTrue(table.primaryKey!!.columns.singleOrNull { it.name == "uuid" } != null)

        // Check mappings

        assertEquals(1, table.mappings.count { it is PropertyMapping.IdentifierMapping })
        assertEquals(3, table.mappings.count { it is PropertyMapping.ValueMapping })

        // Check columns

        assertTrue(table.columns.any { it.name == "uuid" && it.columnType is UUIDColumnType })
        assertTrue(table.columns.any { it.name == "name" && it.columnType is TextColumnType })
        assertTrue(table.columns.any { it.name == "age" && it.columnType is IntegerColumnType })
        assertTrue(table.columns.any { it.name == "password" && it.columnType is TextColumnType && it.columnType.nullable })
    }

    private data class ComplexTestClass (
        val name: String,
        val age: Int,
        val test: TestClass,
        @Invisible val password: String
    ) : Resource()

    @Test fun `should map complex class with values and associations properly`() {
        val table = ClassMapper.mapSingleClass(ComplexTestClass::class)

        println(SqlUtils.dumpOrmTable(table) + "\n")

        // Check PK

        assertNotNull(table.primaryKey)
        assertTrue(table.primaryKey!!.columns.size == 1)
        assertTrue(table.primaryKey!!.columns.first().let { it.name == "uuid" && it.columnType is UUIDColumnType })

        // Check mappings

        assertEquals(1, table.mappings.count { it is PropertyMapping.IdentifierMapping })
        assertEquals(3, table.mappings.count { it is PropertyMapping.ValueMapping })
        assertEquals(1, table.mappings.count { it is PropertyMapping.AssociativeMapping<*> })

        // Check columns

        assertTrue(table.columns.any { it.name == "uuid" && it.columnType is UUIDColumnType })
        assertTrue(table.columns.any { it.name == "name" && it.columnType is TextColumnType })
        assertTrue(table.columns.any { it.name == "age" && it.columnType is IntegerColumnType  })
        assertFalse(table.columns.any { it.name == "test" })
        assertTrue(table.columns.any { it.name == "password" && it.columnType is TextColumnType })

        assertFalse(table.fullyMapped())

        // Check assoc. mapping

        val assocMapping = table.mappings.first { it is PropertyMapping.AssociativeMapping<*> } as PropertyMapping.AssociativeMapping<*>

        assertFalse(assocMapping.complete)
        assertTrue(assocMapping.cardinality == Cardinality.ONE_TO_ONE)
        assertTrue(assocMapping.dependency == TestClass::class)
    }

    @Test fun `should resolve associative mappings properly`() {
        val tables = ClassMapper.mapClasses(TestClass::class, ComplexTestClass::class).toList()
        val testClassTable = tables[0]
        val complexClassTable = tables[1]

        tables.forEach { println(SqlUtils.dumpOrmTable(it) + "\n") }

        assertEquals(0, complexClassTable.dependencyTables.size)

        @Suppress("UNCHECKED_CAST")
        val assocColumn = complexClassTable.mappings.first { it is PropertyMapping.AssociativeMapping<*> }
            .let { (it as PropertyMapping.AssociativeMapping<*>) to complexClassTable.columns.first { col -> col.name == "test" } }

        assertTrue(assocColumn.first.complete)

        // Check FK

        assertNotNull(assocColumn.second.foreignKey)

        val assocColumnFk = assocColumn.second.foreignKey!!

        assertEquals(assocColumn.second, assocColumnFk.from)
        assertEquals(testClassTable.identifyingColumn, assocColumnFk.target)

    }

    private data class TestClassCollections (
        val name: String,
        val age: Int,
        val tests: Set<@CardinalityAnnotation(CollectionCardinality.MANY_TO_MANY) TestClass>,
        @Invisible val password: String
    ) : Resource()

    @Test fun `should resolve associative mappings with resource collection properties properly`() {
        val tables = ClassMapper.mapClasses(TestClass::class, TestClassCollections::class).toList()

        tables.forEach { println(SqlUtils.dumpOrmTable(it) + "\n") }

        val testClassTable = tables[0]
        val collectionClassTable = tables[1]

        // Check associative table

        assertEquals(1, collectionClassTable.dependencyTables.size)

        val assocTable = collectionClassTable.dependencyTables.first()

        // Check assoc. table PK

        assertNotNull(assocTable.primaryKey)
        assertEquals(2, assocTable.primaryKey!!.columns.size)

        assertTrue(assocTable.primaryKey!!.columns.any { it.name == "TestClassCollections" && it.columnType is UUIDColumnType })
        assertTrue(assocTable.primaryKey!!.columns.any { it.name == "TestClass" && it.columnType is UUIDColumnType })

        // Check assoc. table FKs

        assertEquals(2, assocTable.columns.count { it.foreignKey != null })
        val (fk0, fk1) = assocTable.columns.mapNotNull { it.foreignKey }

        assertTrue(fk0.from.table == assocTable)
        assertTrue(fk0.from.name == "TestClassCollections")
        assertTrue(fk0.target.table == collectionClassTable)
        assertTrue(fk0.target.name == "uuid")

        assertTrue(fk1.from.table == assocTable)
        assertTrue(fk1.from.name == "TestClass")
        assertTrue(fk1.target.table == testClassTable)
        assertTrue(fk1.target.name == "uuid")

        // Check assoc. table indices

        assertEquals(0, assocTable.indices.count { it.unique })
    }

    private data class TestClassPrimitiveCollections (
        val name: String,
        val age: Int,
        val testStrings: Set<String>,
        @Invisible val password: String
    ) : Resource()

    @Test fun `should resolve associative mappings with primitive collection properties properly`() {
        val primitiveCollectionClassTable = ClassMapper.mapClasses(TestClassPrimitiveCollections::class).first()

        println(SqlUtils.dumpOrmTable(primitiveCollectionClassTable) + "\n")

        // Check associative table

        assertEquals(1, primitiveCollectionClassTable.dependencyTables.size)

        val assocTable = primitiveCollectionClassTable.dependencyTables.first()

        // Check assoc. table PK

        assertNotNull(assocTable.primaryKey)
        assertEquals(2, assocTable.primaryKey!!.columns.size)

        assertTrue(assocTable.primaryKey!!.columns.any { it.name == "TestClassPrimitiveCollections" && it.columnType is UUIDColumnType })
        assertTrue(assocTable.primaryKey!!.columns.any { it.name == "testStrings" && it.columnType is TextColumnType })

        // Check assoc. table FKs

        assertEquals(1, assocTable.columns.count { it.foreignKey != null })
        val fk0 = assocTable.columns.mapNotNull { it.foreignKey }.first()

        assertTrue(fk0.from.table == assocTable)
        assertTrue(fk0.from.name == "TestClassPrimitiveCollections")
        assertTrue(fk0.target.table == primitiveCollectionClassTable)
        assertTrue(fk0.target.name == "uuid")

        // Check assoc. table indices

        assertEquals(0, assocTable.indices.count { it.unique })
    }

/*    private data class User (
//        @QueryByField
//        @DelegatedSearchReceiver
//        val username: String,
//
//        @Invisible
//        val password: String, // IMPORTANT : DO NOT EVER REMOVE THIS ANNOTATION !
//
//        @QueryByField
//        val name: String,
//
//        val email: String,
//
////        @NoSearch
////        val profilePicture:
////        @type("image/*")
////        @maxByteSize(500_000)
////        StaticResourceHandle,
////
////        @NoSearch
////        val coverPicture:
////        @type("image/*")
////        @maxByteSize(1_000_000)
////        StaticResourceHandle,
//
//        @Invisible
//        val isAdmin: Boolean = false,
//
//        @Undisplayed
//        @SearchDefaultSort
//        val dsf: Int = Random.nextInt(),
//
//        @NoSearch
//        override val uuid: UUID = UUID.randomUUID()
//    ) : Resource(uuid)
//
//    private data class Article (
//
//        @QueryByField
//        val title: @check("^.{0,512}") String,
//
//        @SearchDefaultSort
//        val createdAt: Int = Instant.now().epochSecond.toInt(),
//
//        val createdBy: User,
//
//        @QueryByField
//        val content: String,
//
//        val summary: String,
//
//        @NoSearch
//        val categories: List<String>,
//
//        @NoSearch
//        override val uuid: UUID = UUID.randomUUID()
//
//    ) : Resource(uuid)
//
//    @Test fun a() {
//        val classes = ClassMapper.mapClasses(User::class, Article::class)
//
//        classes.forEach { println(dumpOrmTable(it) + "\n") }
//    } */ */ */

}
