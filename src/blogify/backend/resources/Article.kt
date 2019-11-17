package blogify.backend.resources

import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.ObjectIdGenerators

import blogify.backend.annotations.check
import blogify.backend.annotations.SearchDefaultSort
import blogify.backend.annotations.NoSearch
import blogify.backend.resources.models.Resource

import java.util.*

/**
 * Represents an Article [Resource].
 *
 * @property title      The title of the [Article].
 * @property createdAt  The time of creation of the [Article], in `UNIX` timestamp format.
 * @property createdBy  The UUID of the [User] author of the article.
 * @property content    The content of the article.
 * @property summary    The summary of the article.
 * @property categories The [categories][Article.Category] of the article.
 */
@JsonIdentityInfo (
    scope     = Article::class,
    resolver  = Resource.ObjectResolver::class,
    generator = ObjectIdGenerators.PropertyGenerator::class,
    property  = "uuid"
)
data class Article (

    val title: @check("^.{0,512}") String,

    @SearchDefaultSort
    val createdAt: Long = Date().time,

    @NoSearch
    val createdBy: User,

    val content: String,

    val summary: String,

    @NoSearch
    val categories: List<Category>,

    @NoSearch
    override val uuid: UUID = UUID.randomUUID()
) : Resource(uuid) {

    /**
     * Represents the categories of an [Article].
     *
     * @property name The name content of the category.
     */
    data class Category(val name: String)


}
