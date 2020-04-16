package blogify.backend.resources

import blogify.backend.annotations.Invisible
import blogify.backend.annotations.SqlTable
import blogify.backend.database.Comments
import blogify.backend.database.countReferredToBy
import blogify.backend.pipelines.wrapping.RequestContext
import blogify.backend.events.models.Event
import blogify.backend.events.models.EventSource
import blogify.backend.events.models.EventType
import blogify.backend.resources.computed.compound
import blogify.backend.resources.computed.models.Computed
import blogify.backend.resources.models.Resource
import blogify.backend.resources.models.UserCreatedResource

import com.fasterxml.jackson.annotation.JsonIdentityReference
import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.ObjectIdGenerators
import java.time.Instant

import java.util.UUID

@JsonIdentityInfo (
    scope = Comment::class,
    resolver = Resource.ObjectResolver::class,
    generator = ObjectIdGenerators.PropertyGenerator::class,
    property = "uuid"
)
@SqlTable(Comments::class)
data class Comment (
    @JsonIdentityReference(alwaysAsId = true)
    val commenter: User,

    @JsonIdentityReference(alwaysAsId = true)
    val article: Article,

    @JsonIdentityReference(alwaysAsId = true)
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

    @Invisible
    override val creator = commenter

    // The notification target of a comment is always it's author
    @Invisible
    override val targets = setOf(commenter)

    @Computed
    val likeCount by compound { Comments.uuid countReferredToBy Comments.Likes.comment }

}
