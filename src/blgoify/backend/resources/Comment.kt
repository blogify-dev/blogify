package blgoify.backend.resources

import blgoify.backend.resources.models.Resource

import com.fasterxml.jackson.annotation.JsonIdentityReference

data class Comment (
    @JsonIdentityReference(alwaysAsId = true) val commenter: User,
    @JsonIdentityReference(alwaysAsId = true) val article:   Article,
    val content: String
) : Resource()