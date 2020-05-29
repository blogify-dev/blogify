package blogify.backend.resources

import blogify.reflect.annotations.Hidden
import blogify.backend.annotations.SqlTable
import blogify.reflect.annotations.check
import blogify.backend.database.tables.Articles
import blogify.backend.database.tables.Comments
import blogify.reflect.computed.compound
import blogify.backend.resources.models.Resource
import blogify.backend.database.countReferredToBy
import blogify.backend.events.models.Event
import blogify.backend.events.models.EventType
import blogify.backend.resources.models.UserCreatedResource
import blogify.backend.resources.user.User
import blogify.reflect.computed.models.Computed

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
@SqlTable(Articles::class)
data class Article (

    val title: @check("^.{0,512}") String,

    val createdAt: Int = Instant.now().epochSecond.toInt(),

    val createdBy: User,

    val content: String,

    val summary: String,

    val categories: List<Category>,

    val isDraft: Boolean = false,

    val isPinned: Boolean = false,

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
    data class Category(val name: String)

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
