package blogify.backend.resources.static.models

import io.ktor.http.ContentType

/**
 * Represents an uploaded [StaticData]. Only used while upload.
 *
 * @param contentType the [ContentType] of the uploaded data
 * @param data        the actual byte data
 *
 * @author Benjozork
 */
@Suppress("ArrayInDataClass")
data class StaticData(val contentType: ContentType, val data: ByteArray)
