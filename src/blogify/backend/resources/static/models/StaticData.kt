package blogify.backend.resources.static.models

import io.ktor.http.ContentType

/**
 * Represents an uploaded [StaticData]. Only used while upload.
 *
 * @param contentType the [ContentType] of the uploaded data
 * @param bytes       the actual byte data
 *
 * @author Benjozork
 */
@Suppress("ArrayInDataClass")
open class StaticData(open val contentType: ContentType, open val bytes: ByteArray)
