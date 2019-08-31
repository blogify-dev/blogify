package blgoify.backend.resources

import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.JsonIdentityReference
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.ObjectIdGenerators
import com.fasterxml.jackson.annotation.JsonProperty.Access.*

import blgoify.backend.database.Articles
import blgoify.backend.resources.models.Resource
import blgoify.backend.database.handling.query

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

    val categories: Set<String>,

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

}