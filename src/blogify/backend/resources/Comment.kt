package blogify.backend.resources

import reflectr.annotations.Hidden
import reflectr.computed.compound
import reflectr.computed.models.Computed
import blogify.database.annotations.SqlTable
import blogify.backend.database.tables.Comments
import blogify.backend.database.countReferredToBy
import blogify.backend.pipelines.wrapping.RequestContext
import blogify.backend.events.models.Event
import blogify.backend.events.models.EventSource
import blogify.backend.events.models.EventType
import blogify.backend.resources.models.UserCreatedResource
import blogify.backend.resources.user.User
import reflectr.annotations.search.NoSearch

import java.time.Instant

import java.util.UUID

@NoSearch
@ExperimentalStdlibApi
@SqlTable(Comments::class)
data class Comment (
    val commenter: User,

    val article: Article,

    val parentComment: Comment? = null,

    val content: String,

    val createdAt: Int = Instant.now().epochSecond.toInt(),

    override val uuid: UUID = UUID.randomUUID()

) : UserCreatedResource(uuid), EventSource {

    @Suppress("unused")
    inner class CommentReplyEvent : Event(this.commenter, this, EventType.Notification) {
        val onArticle = this@Comment.article.uuid
        val onComment = this@Comment.parentComment!!.uuid
        val newComment = source.uuid
    }

    inner class CommentCreateEvent : CreationEvent() {
        val article = this@Comment.article
        val commenter = this@Comment.commenter
        val comment = source.uuid
    }

    override suspend fun onCreation(request: RequestContext) {
        if (parentComment != null)
            this.CommentReplyEvent().send(request)
        else
            article.CommentReplyEvent(this).send(request)

        CommentCreateEvent().send(request)
    }

    @Hidden
    override val creator = commenter

    // The notification target of a comment is always it's author
    @Hidden
    override val targets = setOf(commenter)

    @Computed
    val likeCount by compound { Comments.uuid countReferredToBy Comments.Likes.comment }

    override fun equals(other: Any?) = super.equals(other)
    override fun hashCode() = super.hashCode()

}
