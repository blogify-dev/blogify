package blogify.backend.resources

import reflectr.annotations.Hidden
import reflectr.annotations.check
import blogify.backend.search.annotations.*
import reflectr.computed.compound
import reflectr.computed.models.Computed
import krate.annotations.SqlTable
import blogify.backend.database.tables.Articles
import blogify.backend.database.tables.Comments
import blogify.backend.entity.Resource
import blogify.backend.database.countReferredToBy
import blogify.backend.events.models.Event
import blogify.backend.events.models.EventType
import blogify.backend.resources.models.UserCreatedResource
import blogify.backend.resources.user.User
import blogify.backend.search.annotations.DelegatedSearch
import reflectr.annotations.search.NoSearch

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
@ExperimentalStdlibApi
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
    val isDraft: Boolean = false,

    @NoSearch
    val isPinned: Boolean = false,

    val isHidden: Boolean = false,

    @NoSearch
    override val uuid: UUID = UUID.randomUUID()

) : UserCreatedResource(uuid) {

    inner class CommentReplyEvent(comment: Comment) : Event(comment.commenter, this, EventType.Notification) {
        val onArticle = source.uuid
        val newComment = comment.uuid
    }

    /**
     * Represents the categories of an [Article].
     *
     * @property name The name content of the category.
     */
    data class Category(@DelegatedSearchReceiver val name: String)

    @Hidden
    override val creator = createdBy

    // The notification target of an article is always it's author
    @Hidden
    override val targets = setOf(createdBy)

    @Computed
    val likeCount by compound { Articles.uuid countReferredToBy Articles.Likes.article }

    @Computed
    val commentCount by compound { Articles.uuid countReferredToBy Comments.article }

    override fun equals(other: Any?) = super.equals(other)
    override fun hashCode() = super.hashCode()

}
