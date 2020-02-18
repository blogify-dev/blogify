package blogify.backend.resources

import blogify.backend.annotations.search.NoSearch
import blogify.backend.database.Comments
import blogify.backend.database.referredToBy
import blogify.backend.resources.computed.compound
import blogify.backend.resources.computed.models.Computed
import com.fasterxml.jackson.annotation.JsonIdentityReference
import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.ObjectIdGenerators

import blogify.backend.resources.models.Resource

import java.util.UUID

@JsonIdentityInfo(
    scope = Comment::class,
    resolver = Resource.ObjectResolver::class,
    generator = ObjectIdGenerators.PropertyGenerator::class,
    property = "uuid"
)
data class Comment(
    @JsonIdentityReference(alwaysAsId = true)
    val commenter: User,

    @JsonIdentityReference(alwaysAsId = true)
    val article: Article,

    @JsonIdentityReference(alwaysAsId = true)
    val parentComment: Comment? = null,

    val content: String,

    override val uuid: UUID = UUID.randomUUID()
) : Resource(uuid) {

    @[Computed NoSearch]
    val likeCount by compound { Comments.uuid referredToBy Comments.Likes.comment }

}
