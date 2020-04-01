package blogify.backend.persistence.postgres.orm.query

import blogify.backend.persistence.postgres.orm.query.extensions.then
import blogify.backend.persistence.postgres.orm.query.models.PropertyGraph
import blogify.backend.persistence.postgres.orm.query.models.Pointer
import blogify.backend.resources.Article
import blogify.backend.resources.Comment
import blogify.backend.resources.User
import blogify.backend.resources.models.Resource
import blogify.backend.resources.reflect.cachedPropMap
import blogify.backend.resources.reflect.models.ext.okHandle
import blogify.backend.testutils.PropertyGraphUtils

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

import kotlin.time.ExperimentalTime
import kotlin.time.measureTime
import kotlin.time.milliseconds

import com.andreapivetta.kolor.lightBlue
import com.andreapivetta.kolor.yellow

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

    @ExperimentalTime
    @Test fun `should make graph with simple pointers`() {
        House::class.cachedPropMap()
        Owner::class.cachedPropMap()
        Cat::class.cachedPropMap()

        val timeTaken = measureTime {
            val houseOccupant = Pointer<House, House, Owner>(null, House::occupant)
            val houseOccupantName = houseOccupant then Owner::name
            val houseOccupantCat = houseOccupant then Owner::cat
            val houseOccupantCatName = houseOccupantCat then Cat::name
            val houseOccupantCatBreed = houseOccupantCat then Cat::breed

            val graph = PropertyGraph (
                House::class,
                houseOccupant,
                houseOccupantName,
                houseOccupantCat,
                houseOccupantCatName,
                houseOccupantCatBreed
            )

            println(PropertyGraphUtils.dumpPropertyGraph(graph))

            assertTrue(graph.rootChildren.map { it.pointer.handle.name }.containsAll(listOf("occupant")))
            assertTrue(graph.rootChildren.first().children.map { it.pointer.handle.name }.containsAll(listOf("name", "cat")))
            assertTrue(graph.rootChildren.first().children.first { it.pointer.handle.name == "cat" }.children.map { it.pointer.handle.name }
                .containsAll(listOf("name", "breed")))
        }

        println("Time taken : ".yellow() + timeTaken.toString().lightBlue())

        assertTrue(timeTaken < 10.milliseconds)
    }

    private data class PartnerContainer (
        val contained: Partner
    ) : Resource()

    private data class Partner (
        val name: String,
        val spouse: Partner?
    ) : Resource()

    @Test fun `should make graph with circular reference pointers`() {
        val partner = Pointer<PartnerContainer, PartnerContainer, Partner>(null, PartnerContainer::contained.okHandle())
        val partnerName = Pointer<PartnerContainer, Partner, String>(partner, Partner::name.okHandle())
        val partnerSpouse = Pointer<PartnerContainer, Partner, Partner>(partner, Partner::spouse.okHandle())
        val partnerSpouseName = Pointer<PartnerContainer, Partner, String>(partnerSpouse, Partner::name.okHandle())

        val graph = PropertyGraph (
            PartnerContainer::class,
            partner,
            partnerName,
            partnerSpouse,
            partnerSpouseName
        )

        println(PropertyGraphUtils.dumpPropertyGraph(graph))

        assertTrue(graph.rootChildren.map { it.pointer.handle.name }.containsAll(listOf("contained")))
        assertTrue(graph.rootChildren.first().children.map { it.pointer.handle.name }.containsAll(listOf("name", "spouse")))
        assertTrue(graph.rootChildren.first().children.first { it.pointer.handle.name == "spouse" }.children.map { it.pointer.handle.name }
            .contains("name"))
    }

    @Test fun a() {
        class CommentContainer(val comment: Comment) : Resource()
        val comment = Pointer<CommentContainer, CommentContainer, Comment>(null, CommentContainer::comment)
        val commentAuthor = comment then Comment::commenter
        val commentAuthorUsername = commentAuthor then User::username
        val commentArticle = comment then Comment::article
        val commentArticleTitle = commentArticle then Article::title

        val graph = PropertyGraph (
            CommentContainer::class,
            comment,
            commentAuthor,
            commentAuthorUsername,
            commentArticle,
            commentArticleTitle
        )

        println(PropertyGraphUtils.dumpPropertyGraph(graph))
    }

}
