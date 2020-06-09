package blogify.backend.database.tables

import blogify.backend.database.handling.query
import blogify.backend.database.models.QueryContext
import blogify.backend.resources.static.image.ImageMetadata
import blogify.common.util.Sr
import blogify.common.util.Wrap

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.select

object ImageUploadablesMetadata : Table("image_metadata") {

    val handleId = varchar ("id", 32).references(Uploadables.fileId, onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.RESTRICT)
    val width    = integer ("width").default(0)
    val height   = integer ("height").default(0)

    suspend fun obtain(queryContext: QueryContext, id: String): Sr<ImageMetadata> =
        Wrap {
            query { this.select { handleId eq id }.single() }.get()
                .let {
                    convert(
                        queryContext,
                        it
                    ).get()
                }
        }

    private suspend fun convert(@Suppress("UNUSED_PARAMETER") queryContext: QueryContext, source: ResultRow) =
        Wrap {
            ImageMetadata(
                source[width],
                source[height]
            )
        }

}
