package blogify.backend.resources.reflect.extensions

import blogify.backend.appContext
import blogify.reflect.models.Mapped
import blogify.reflect.sanitize

/**
 * Runs [Mapped.sanitize] and turns the resulting [blogify.reflect.Dto] into a JSON string
 *
 * @author Benjozork
 */
fun <M : Mapped> M.sanitizeToString(): String =
        appContext.objectMapper.writeValueAsString(this.sanitize())
