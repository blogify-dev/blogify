package blgoify.backend.database

import blgoify.backend.resources.Article

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table

object Articles : Table() {

    val uuid      = uuid    ("uuid").primaryKey()
    val title     = varchar ("title", 512)
    val createdAt = long    ("created_at")

    fun toArticle(source: ResultRow): Article {
        return Article (
            title     = source[title],
            createdAt = source[createdAt],
            content   = Article.Content("test"),
            uuid      = source[uuid]
        )
    }

}