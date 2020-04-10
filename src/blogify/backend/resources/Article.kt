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
import blogify.backend.database.countReferredToBy
import blogify.backend.events.models.Event
import blogify.backend.resources.models.UserCreatedResource

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

) : UserCreatedResource(uuid) {

    inner class CommentReplyEvent(comment: Comment) : Event(comment.commenter, this) {
        val onArticle = source.uuid
        val newComment = comment.uuid
    }

    /**
     * Represents the categories of an [Article].
     *
     * @property name The name content of the category.
     */
    data class Category(@DelegatedSearchReceiver val name: String)

    @Invisible
    override val creator = createdBy

    // The notification target of an article is always it's author
    @Invisible
    override val targets = setOf(createdBy)

    @Computed
    val likeCount by compound { Articles.uuid countReferredToBy Articles.Likes.article }

    @Computed
    val commentCount by compound { Articles.uuid countReferredToBy Comments.article }

}
