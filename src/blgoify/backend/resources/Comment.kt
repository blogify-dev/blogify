package blgoify.backend.resources

import com.fasterxml.jackson.annotation.JsonIdentityReference
import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.ObjectIdGenerators

import blgoify.backend.resources.models.Resource

import java.util.UUID

@JsonIdentityInfo (
    scope     = Comment::class,
    resolver  = Resource.ObjectResolver::class,
    generator = ObjectIdGenerators.PropertyGenerator::class,
    property  = "uuid"
)
data class Comment (
    @JsonIdentityReference(alwaysAsId = true)
    val commenter: User,

    @JsonIdentityReference(alwaysAsId = true)
    val article: Article,

    val content: String,

    override val uuid: UUID = UUID.randomUUID()
) : Resource(uuid)