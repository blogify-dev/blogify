package blgoify.backend.resources

import com.fasterxml.jackson.annotation.JsonIdentityReference

import blgoify.backend.resources.models.Resource

import java.util.UUID

data class Comment (
    @JsonIdentityReference(alwaysAsId = true)
    val commenter: User,

    @JsonIdentityReference(alwaysAsId = true)
    val article: Article,

    val content: String,

    override val uuid: UUID = UUID.randomUUID()
) : Resource(uuid)