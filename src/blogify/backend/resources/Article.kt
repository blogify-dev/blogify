package blogify.backend.resources

import blogify.backend.annotations.Invisible
import blogify.backend.annotations.SqlTable
import blogify.backend.annotations.check
import blogify.backend.annotations.search.*
import blogify.backend.database.Articles
import blogify.backend.database.Comments
import blogify.backend.resources.computed.compound
import blogify.backend.resources.computed.models.Computed
import blogify.backend.resources.models.Resource
import blogify.backend.database.referredToBy
import blogify.backend.notifications.models.NotificationSource

import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.ObjectIdGenerators

import java.time.Instant
import java.util.UUID

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
@SqlTable(Articles::class)
data class Article (

    @QueryByField
    val title: @check("^.{0,512}") String,

    @SearchDefaultSort
    val createdAt: Int = Instant.now().epochSecond.toInt(),

    val createdBy: @DelegatedSearch User,

    @QueryByField
    val content: String,

    val summary: String,

    @NoSearch
    val categories: @DelegatedSearch List<Category>,

    @NoSearch
    override val uuid: UUID = UUID.randomUUID()

) : Resource(uuid), NotificationSource {

    /**
     * Represents the categories of an [Article].
     *
     * @property name The name content of the category.
     */
    data class Category(@DelegatedSearchReceiver val name: String)

    // The notification target of an article is always it's author
    @Invisible
    override val targets = setOf(createdBy)

    @[Computed NoSearch]
    val likeCount by compound { Articles.uuid referredToBy Articles.Likes.article }

    @[Computed NoSearch]
    val commentCount by compound { Articles.uuid referredToBy Comments.article }

}
