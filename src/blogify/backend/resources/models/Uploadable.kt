package blogify.backend.resources.models

import blogify.backend.util.noslice

import io.ktor.http.ContentType

import com.fasterxml.jackson.annotation.JsonIgnore

data class Uploadable (
    @JsonIgnore @noslice
    val longId:      Long,
    val contentType: ContentType
) {

    val id get() = longId.toString(16)

}
