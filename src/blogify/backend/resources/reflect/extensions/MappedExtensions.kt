package blogify.backend.resources.reflect.extensions

import blogify.backend.appContext
import reflectr.models.Mapped
import reflectr.sanitize

/**
 * Runs [Mapped.sanitize] and turns the resulting [blogify.reflect.Dto] into a JSON string
 *
 * @author Benjozork
 */
@ExperimentalStdlibApi
fun <M : Mapped> M.sanitizeToString(): String =
        appContext.objectMapper.writeValueAsString(this.sanitize())
