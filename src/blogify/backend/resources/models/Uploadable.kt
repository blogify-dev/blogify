package blogify.backend.resources.models

import io.ktor.http.ContentType

data class Uploadable (
    val id:          Long,
    val collection:  Collection,
    val contentType: ContentType
)
