package blogify.backend.resources

import blogify.backend.annotations.Invisible
import blogify.backend.annotations.SqlTable
import blogify.backend.annotations.search.NoSearch
import blogify.backend.database.Comments
import blogify.backend.database.countReferredToBy
import blogify.backend.pipelines.wrapping.RequestContext
import blogify.backend.events.models.Event
import blogify.backend.events.models.EventSource
import blogify.backend.push.Message
import blogify.backend.resources.computed.compound
import blogify.backend.resources.computed.models.Computed
import blogify.backend.resources.models.Resource

import com.fasterxml.jackson.annotation.JsonIdentityReference
import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.ObjectIdGenerators

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

    override val uuid: UUID = UUID.randomUUID()

) : Resource(uuid), EventSource {

    inner class CommentReplyEvent : Event(this.commenter, this) {
        val onArticle = this@Comment.article.uuid
        val onComment = this@Comment.parentComment!!.uuid
        val newComment = source.uuid
    }

    override suspend fun onCreation(request: RequestContext) {
        if (parentComment != null)
            this.CommentReplyEvent().send(request)
        else
            article.CommentReplyEvent(this).send(request)

        request.appContext.pushServer.sendMessageToAllConnected(Message.Outgoing.ActivityNotification(this))
    }

    // The notification target of a comment is always it's author
    @Invisible
    override val targets = setOf(commenter)

    @Computed
    val likeCount by compound { Comments.uuid countReferredToBy Comments.Likes.comment }

}
