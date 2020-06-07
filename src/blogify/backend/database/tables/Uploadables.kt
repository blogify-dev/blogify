package blogify.backend.database.tables

import blogify.backend.database.models.QueryContext
import blogify.backend.resources.static.image.ImageMetadata
import blogify.backend.resources.static.models.StaticFile
import blogify.backend.util.Wrap
import blogify.backend.util.matches

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table

import com.github.kittinunf.result.coroutines.getOrElse

import io.ktor.http.ContentType

object Uploadables : Table() {

    val fileId      = varchar ("id", 32)
    val contentType = varchar ("content_type", 64)

    override val primaryKey = PrimaryKey(fileId)

    suspend fun convert(@Suppress("UNUSED_PARAMETER") queryContext: QueryContext, source: ResultRow) =
        Wrap {

            // Parse the content type
            val contentType = ContentType.parse(source[contentType])

            return@Wrap if (contentType matches ContentType.Image.Any) { // If it's an image, read metadata
                StaticFile.Ok.Image(
                    contentType = contentType,
                    fileId = source[fileId],
                    metadata = ImageUploadablesMetadata.obtain(
                        queryContext,
                        source[fileId]
                    )
                        .getOrElse(ImageMetadata(0, 0))
                )
            } else { // If not, just return a normal handle
                StaticFile.Ok(
                    contentType = contentType,
                    fileId = source[fileId]
                )
            }
        }

}
