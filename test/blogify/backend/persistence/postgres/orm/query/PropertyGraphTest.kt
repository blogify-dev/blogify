package blogify.backend.persistence.postgres.orm.query

import blogify.backend.persistence.postgres.orm.annotations.Cardinality
import blogify.backend.persistence.postgres.orm.extensions.mappedTable
import blogify.backend.persistence.postgres.orm.models.CollectionCardinality
import blogify.backend.persistence.postgres.orm.query.extensions.then
import blogify.backend.persistence.postgres.orm.query.extensions.thenEach
import blogify.backend.persistence.postgres.orm.query.models.CollectionPointer
import blogify.backend.persistence.postgres.orm.query.models.PropertyGraph
import blogify.backend.persistence.postgres.orm.query.models.Pointer
import blogify.backend.resources.models.Resource
import blogify.backend.resources.reflect.cachedPropMap
import blogify.backend.resources.reflect.models.ext.okHandle
import blogify.backend.testutils.*

import org.jetbrains.exposed.sql.Join

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertEquals

import kotlin.time.ExperimentalTime
import kotlin.time.measureTime
import kotlin.time.milliseconds

import com.andreapivetta.kolor.lightBlue
import com.andreapivetta.kolor.red
import com.andreapivetta.kolor.yellow

class PropertyGraphTest {

    private data class House (
        val size: Int,
        val occupant: Owner
    ) : Resource()

    private data class Owner (
        val name: String,
        val cat: Cat
    ) : Resource()

    private data class Cat (
        val name: String,
        val breed: String
    ) : Resource()

    @ExperimentalTime
    @Test fun `should make graph and join with simple pointers`() {
        Cat::class.mappedTable
        Owner::class.mappedTable
        House::class.cachedPropMap()
        House::class.mappedTable
        Owner::class.cachedPropMap()
        Cat::class.cachedPropMap()

        var graph: PropertyGraph<House>? = null

        val timeTaken = measureTime {
            for (i in 1..10) {
                val houseOccupant = Pointer<House, House, Owner>(null, House::occupant)
                val houseOccupantName = houseOccupant then Owner::name
                val houseOccupantCat = houseOccupant then Owner::cat
                val houseOccupantCatName = houseOccupantCat then Cat::name
                val houseOccupantCatBreed = houseOccupantCat then Cat::breed

                graph = PropertyGraph (
                    House::class,
                    houseOccupant,
                    houseOccupantName,
                    houseOccupantCat,
                    houseOccupantCatName,
                    houseOccupantCatBreed
                )
            }
        } / 10

        // Check time taken

        println(PropertyGraphUtils.dumpPropertyGraph(graph!!) + "\n")

        println("Time taken : ".yellow() + timeTaken.toString().lightBlue())

        assertTrue(timeTaken < 1.milliseconds, "took >1ms")

        // Check graph

        assertTrue(graph!!.rootChildren.map { it.pointer.handle.name }.containsAll(listOf("occupant")))
        assertTrue(graph!!.rootChildren.first().children.map { it.pointer.handle.name }.containsAll(listOf("name", "cat")))
        assertTrue(graph!!.rootChildren.first().children.first { it.pointer.handle.name == "cat" }.children.map { it.pointer.handle.name }
            .containsAll(listOf("name", "breed")))

        // Check generated join

        val join = graph!!.toJoin()

        println(SqlUtils.dumpJoin(graph!!.toJoin()) + "\n")

        // Check generated join

        val joinTable = join.let {
            var table = it.table
            while (table is Join) {
                table = table.table
            }
            table
        }

        val columnDumps = arrayOf (
            Regex ("House.*uuid.*UUIDColumnType"),
            Regex ("House.*size.*IntegerColumnType"),
            Regex ("House.*occupant.*UUIDColumnType"),
            Regex ("joined_ptr_\\w+.*uuid.*UUIDColumnType"),
            Regex ("joined_ptr_\\w+.*name.*TextColumnType"),
            Regex ("joined_ptr_\\w+.*cat.*UUIDColumnType"),
            Regex ("joined_ptr_\\w+.*uuid.*UUIDColumnType"),
            Regex ("joined_ptr_\\w+.*name.*TextColumnType"),
            Regex ("joined_ptr_\\w+.*breed.*TextColumnType")
        )

        assertEquals(House::class.mappedTable, joinTable)
        join.columns.map {SqlUtils.dumpColumn(it, true) }.forEach {
            assertTrue(columnDumps.any { r -> it.contains(r) }, "$it doesn't match".red())
        }
    }

    private data class PartnerContainer (
        val contained: Partner
    ) : Resource()

    private data class Partner (
        val name: String,
        val spouse: Partner?
    ) : Resource()

    @ExperimentalTime
    @Test fun `should make graph and join with circular reference pointers`() {
        Partner::class.cachedPropMap()
        Partner::class.mappedTable

        var graph: PropertyGraph<PartnerContainer>? = null

        val timeTaken = measureTime {
            for (i in 1..10) {
                val partner =
                    Pointer<PartnerContainer, PartnerContainer, Partner>(null, PartnerContainer::contained.okHandle())
                val partnerName = partner then Partner::name
                val partnerSpouse = Pointer<PartnerContainer, Partner, Partner>(partner, Partner::spouse.okHandle())
                val partnerSpouseName = partnerSpouse then Partner::name

                graph = PropertyGraph(
                    PartnerContainer::class,
                    partner,
                    partnerName,
                    partnerSpouse,
                    partnerSpouseName
                )
            }
        } / 10

        println(PropertyGraphUtils.dumpPropertyGraph(graph!!) + "\n")

        // Check time taken

        println("Time taken : ".yellow() + timeTaken.toString().lightBlue())

        assertTrue(timeTaken < 1.milliseconds, "took >1ms")

        // Check graph

        assertTrue(graph!!.rootChildren.map { it.pointer.handle.name }.containsAll(listOf("contained")))
        assertTrue(graph!!.rootChildren.first().children.map { it.pointer.handle.name }
            .containsAll(listOf("name", "spouse")))
        assertTrue(graph!!.rootChildren.first().children.first { it.pointer.handle.name == "spouse" }.children.map { it.pointer.handle.name }
            .contains("name"))

        val join = graph!!.toJoin()

        println(SqlUtils.dumpJoin(graph!!.toJoin()) + "\n")

        // Check generated join

        val joinTable = join.let {
            var table = it.table
            while (table is Join) {
                table = table.table
            }
            table
        }

        val columnDumps = arrayOf (
            Regex ("PartnerContainer.*uuid.*UUIDColumnType"),
            Regex ("PartnerContainer.*contained.*UUIDColumnType"),
            Regex ("joined_ptr_\\w+.*uuid.*UUIDColumnType"),
            Regex ("joined_ptr_\\w+.*name.*TextColumnType"),
            Regex ("joined_ptr_\\w+.*spouse\\?.*UUIDColumnType"),
            Regex ("joined_ptr_\\w+.*name.*TextColumnType"),
            Regex ("joined_ptr_\\w+.*uuid.*UUIDColumnType"),
            Regex ("joined_ptr_\\w+.*spouse\\?.*UUIDColumnType")
        )

        assertEquals(PartnerContainer::class.mappedTable, joinTable)
        join.columns.map { SqlUtils.dumpColumn(it, true) }.forEach {
            assertTrue(columnDumps.any { r -> it.contains(r) }, "$it doesn't match".red())
        }
    }

    private data class Jar (
        val cookies: Set<@Cardinality(CollectionCardinality.ONE_TO_MANY) Cookie>
    ) : Resource()

    private data class Cookie (
        val flavor: String,
        val color: String,
        val size: Int
    ) : Resource()

    @ExperimentalTime
    @Test fun `should make graph and join with collection pointers`() {
        Cookie::class.mappedTable

        var graph: PropertyGraph<Jar>? = null

        val timeTaken = measureTime {
            for (i in 1..10) {
                val cookies = CollectionPointer<Jar, Jar, Cookie>(null, Jar::cookies)
                val cookiesFlavor = cookies thenEach Cookie::flavor

                graph = PropertyGraph (
                    Jar::class,
                    cookies,
                    cookiesFlavor
                )
            }
        } / 10

        println(PropertyGraphUtils.dumpPropertyGraph(graph!!) + "\n")

        // Check time taken

        println("Time taken : ".yellow() + timeTaken.toString().lightBlue())

        assertTrue(timeTaken < 2.milliseconds, "took >2ms")

        // Check graph

        assertTrue(graph!!.rootChildren.map { it.pointer.handle.name }.contains("cookies"))
        assertTrue(graph!!.rootChildren.first().children.map { it.pointer.handle.name }
            .contains("flavor"))

        val join = graph!!.toJoin()

        println(SqlUtils.dumpJoin(graph!!.toJoin()) + "\n")

        // Check generated join

        val joinTable = join.let {
            var table = it.table
            while (table is Join) {
                table = table.table
            }
            table
        }

        val columnDumps = arrayOf (
            Regex ("Jar.*uuid.*UUIDColumnType"),
            Regex ("Jar_uuid_to_Cookie_uuid.*Cookie.*UUIDColumnType"),
            Regex ("Jar_uuid_to_Cookie_uuid.*uuid.*UUIDColumnType"),
            Regex ("Cookie.*uuid.*UUIDColumnType"),
            Regex ("Cookie.*color.*TextColumnType"),
            Regex ("Cookie.*flavor.*TextColumnType"),
            Regex ("Cookie.*size.*IntegerColumnType")
        )

        assertEquals(Jar::class.mappedTable, joinTable)
        join.columns.map { SqlUtils.dumpColumn(it, true) }.forEach {
            assertTrue(columnDumps.any { r -> it.contains(r) }, "$it doesn't match".red())
        }
    }

}
