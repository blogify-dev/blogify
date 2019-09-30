package blogify.backend.resources

import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.JsonIdentityReference
import com.fasterxml.jackson.annotation.ObjectIdGenerators

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

    val content: String,

    val summary: String,

    val categories: List<Category>,

    override val uuid: UUID = UUID.randomUUID()
) : Resource(uuid) {

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
