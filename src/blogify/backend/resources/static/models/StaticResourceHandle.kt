package blogify.backend.resources.static.models

import blogify.backend.resources.static.image.ImageMetadata

import io.ktor.http.ContentType

/**
 * Represents a pointer to an uploaded file (or lack thereof)
 *
 * @param contentType the content type of files this handle can point to
 *
 * @author Benjozork
 */
sealed class StaticResourceHandle(open val contentType: ContentType) {

    /**
     * Represents a pointer to a file that hasn't been uploaded yet
     *
     * @param contentType the content type of files this handle can point to
     */
    class None(contentType: ContentType): StaticResourceHandle(contentType) {
        override fun toString(): String = "None(contentType=$contentType)"
    }

    /**
     * Represents a pointer to an existing file
     *
     * @param fileId the file ID to make it point to
     */
    open class Ok(contentType: ContentType, val fileId: String): StaticResourceHandle(contentType) {
        override fun toString(): String = "Ok(contentType=$contentType, fileId=$fileId)"

        class Image(val metadata: ImageMetadata, contentType: ContentType, fileId: String): Ok(contentType, fileId)
    }

}
