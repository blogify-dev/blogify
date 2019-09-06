package blogify.backend.resources

import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.JsonIdentityReference
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.ObjectIdGenerators
import com.fasterxml.jackson.annotation.JsonProperty.Access.*

import blogify.backend.database.Articles
import blogify.backend.resources.models.Resource
import blogify.backend.database.handling.query

import org.jetbrains.exposed.sql.select

import java.util.*

/**
 * Represents an Article [Resource].
 *
 * @property title     The title of the [Article].
 * @property createdAt The time of creation of the [Article], in `UNIX` timestamp format.
 * @property createdBy The UUID of the [User] author of the article.
 * @property content   The [Content][Article.Content] of the article. Not included in the JSON serialization.

 */
@JsonIdentityInfo (
    scope     = Article::class,
    resolver  = Resource.ObjectResolver::class,
    generator = ObjectIdGenerators.PropertyGenerator::class,
    property  = "uuid"
)
data class Article (
    val title: String,
    val createdAt: Long = Date().time,

    @JsonIdentityReference(alwaysAsId = true)
    val createdBy: User,

    @JsonProperty(access = WRITE_ONLY)
    val content: Content?,

    val categories: List<Category>,

    override val uuid: UUID = UUID.randomUUID()
) : Resource(uuid) {

    /**
     * Represents the content of an [Article].
     *
     * @property text    The text content of the article.
     * @property summary The summary of the content.
     */
    data class Content(val text: String, val summary: String)

    suspend fun content(): Content = query {
        Articles.Content.select {
            Articles.Content.article eq this@Article.uuid
        }.single().let { Articles.Content.convert(it) }
    }.get()

    /**
     * Represents the categories of an [Article].
     *
     * @property name The name content of the category.
     */
    data class Category(val name: String)

    suspend fun category(): List<Category> = query {
        Articles.Categories.select {
            Articles.Categories.article eq this@Article.uuid
        }.toList().map{ Articles.Categories.convert(it) }
    }.get()

}