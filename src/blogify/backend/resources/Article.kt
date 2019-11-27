package blogify.backend.resources

import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.ObjectIdGenerators

import blogify.backend.annotations.check
import blogify.backend.annotations.search.NoSearch
import blogify.backend.annotations.search.SearchByUUID
import blogify.backend.annotations.search.SearchDefaultSort
import blogify.backend.annotations.search.DelegatedSearch
import blogify.backend.annotations.search.DelegatedSearchReceiver
import blogify.backend.resources.models.Resource

import java.util.*
import kotlin.random.Random

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

    val createdAt: Long = Date().time,

    val createdBy: @DelegatedSearch User,

    val content: String,

    val summary: String,

    @NoSearch
    val categories: @DelegatedSearch List<Category>,

    @SearchDefaultSort
    val dsf: Int = Random.nextInt(),

    @NoSearch
    override val uuid: UUID = UUID.randomUUID()
) : Resource(uuid) {

    /**
     * Represents the categories of an [Article].
     *
     * @property name The name content of the category.
     */
    data class Category(@DelegatedSearchReceiver val name: String)

}
