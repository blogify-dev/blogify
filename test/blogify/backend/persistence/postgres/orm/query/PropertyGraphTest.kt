package blogify.backend.persistence.postgres.orm.query

import blogify.backend.persistence.postgres.orm.query.models.PropertyGraph
import blogify.backend.persistence.postgres.orm.query.models.PropertyPointer
import blogify.backend.resources.models.Resource
import blogify.backend.resources.reflect.models.ext.okHandle

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertTrue

class PropertyGraphTest {

    private data class House (
        val size: Pair<Int, Int>,
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

    @Test fun `should make graph with simple pointers`() {
        val houseOccupant = PropertyPointer<House, House, Owner>(null, House::occupant.okHandle())
        val houseOccupantName = PropertyPointer<House, Owner, String>(houseOccupant, Owner::name.okHandle())
        val houseOccupantCat = PropertyPointer<House, Owner, Cat>(houseOccupant, Owner::cat.okHandle())
        val houseOccupantCatName = PropertyPointer<House, Cat, String>(houseOccupantCat, Cat::name.okHandle())
        val houseOccupantCatBreed = PropertyPointer<House, Cat, String>(houseOccupantCat, Cat::breed.okHandle())

        val graph = PropertyGraph (
            House::class,
            houseOccupant,
            houseOccupantName,
            houseOccupantCat,
            houseOccupantCatName,
            houseOccupantCatBreed
        )

        assertTrue(graph.rootChildren.map { it.pointer.handle.name }.containsAll(listOf("occupant")))
        assertTrue(graph.rootChildren.first().children.map { it.pointer.handle.name }.containsAll(listOf("name", "cat")))
        assertTrue(graph.rootChildren.first().children.first { it.pointer.handle.name == "cat" }.children.map { it.pointer.handle.name }
            .containsAll(listOf("name", "breed")))
    }

    private data class PartnerContainer (
        val contained: Partner
    ) : Resource()

    private data class Partner (
        val name: String,
        val spouse: Partner?
    ) : Resource()

    @Test fun `should make graph with complex pointers`() {
        val partner = PropertyPointer<PartnerContainer, PartnerContainer, Partner>(null, PartnerContainer::contained.okHandle())
        val partnerName = PropertyPointer<PartnerContainer, Partner, String>(partner, Partner::name.okHandle())
        val partnerSpouse = PropertyPointer<PartnerContainer, Partner, Partner>(partner, Partner::spouse.okHandle())
        val partnerSpouseName = PropertyPointer<PartnerContainer, Partner, String>(partnerSpouse, Partner::name.okHandle())

        val graph = PropertyGraph (
            PartnerContainer::class,
            partner,
            partnerName,
            partnerSpouse,
            partnerSpouseName
        )

        assertTrue(graph.rootChildren.map { it.pointer.handle.name }.containsAll(listOf("contained")))
        assertTrue(graph.rootChildren.first().children.map { it.pointer.handle.name }.containsAll(listOf("name", "spouse")))
        assertTrue(graph.rootChildren.first().children.first { it.pointer.handle.name == "spouse" }.children.map { it.pointer.handle.name }
            .contains("name"))
    }

}
