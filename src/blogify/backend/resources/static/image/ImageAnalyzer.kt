package blogify.backend.resources.static.image

import blogify.backend.resources.static.models.StaticData

import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.Metadata
import com.drew.metadata.exif.ExifImageDirectory

import io.ktor.http.ContentType

class ImageData private constructor (
    override val contentType: ContentType,
    override val bytes: ByteArray,

    val metadata: Metadata? = try {
        ImageMetadataReader.readMetadata(bytes.inputStream())
    } catch (_: Exception) { null }

) : StaticData(contentType, bytes)

fun StaticData.imageHandle(): ImageData? =
    if (this.contentType.match("image/*")) this as ImageData else null

fun ImageData.width()
        = this.metadata?.getFirstDirectoryOfType(ExifImageDirectory::class.java)?.getInt(ExifImageDirectory.TAG_IMAGE_WIDTH)
    ?: 0

fun ImageData.height()
        = this.metadata?.getFirstDirectoryOfType(ExifImageDirectory::class.java)?.getInt(ExifImageDirectory.TAG_IMAGE_HEIGHT)
    ?: 0
