package blogify.backend.database.tables

import blogify.backend.database.handling.query
import blogify.backend.pipelines.wrapping.RequestContext
import blogify.backend.resources.static.image.ImageMetadata
import blogify.backend.util.Sr
import blogify.backend.util.Wrap

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.select

object ImageUploadablesMetadata : Table("image_metadata") {

    val handleId = varchar ("id", 32).references(Uploadables.fileId, onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.RESTRICT)
    val width    = integer ("width").default(0)
    val height   = integer ("height").default(0)

    suspend fun obtain(requestContext: RequestContext, id: String): Sr<ImageMetadata> =
        Wrap {
            query { this.select { handleId eq id }.single() }.get()
                .let {
                    convert(
                        requestContext,
                        it
                    ).get()
                }
        }

    private suspend fun convert(@Suppress("UNUSED_PARAMETER") requestContext: RequestContext, source: ResultRow) =
        Wrap {
            ImageMetadata(
                source[width],
                source[height]
            )
        }

}
