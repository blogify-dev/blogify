package blgoify.backend.database

import blgoify.backend.resources.Article
import blgoify.backend.resources.Comment
import blgoify.backend.resources.User
import blgoify.backend.resources.models.Resource
import blgoify.backend.services.articles.ArticleService
import blgoify.backend.services.UserService

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table

abstract class ResourceTable<R : Resource> : Table() {

    abstract suspend fun convert(source: ResultRow): R

}

object Articles : ResourceTable<Article>() {

    val uuid      = uuid    ("uuid").primaryKey()
    val title     = varchar ("title", 512)
    val createdAt = long    ("created_at")

    override suspend fun convert(source: ResultRow) = Article (
        uuid      = source[uuid],
        title     = source[title],
        createdAt = source[createdAt],
        content = Article.Content("This is text", "summ") // Temporary. following is the real code
        /*transaction {
            Content.select { Content.article eq source[uuid] }
        }.mapNotNull { Content.convert(it) }.singleOrNull() ?: error("no content in db for article ${source[uuid]}")*/
    )

    object Content : Table() {

        val article = uuid ("article").primaryKey()
        val text    = text ("text")
        val summary = text ("summary")

        @Suppress("RedundantSuspendModifier")
        suspend fun convert(source: ResultRow) = Article.Content (
            text    = source[text],
            summary = source[summary]
        )

    }

    }

}

object Comments : ResourceTable<Comment>() {

    val uuid      = uuid ("uuid").primaryKey()
    val commenter = uuid ("commenter")
    val article   = uuid ("article")
    val content   = text ("content")

    override suspend fun convert(source: ResultRow) = Comment (
        uuid      = source[uuid],
        content   = source[content],
        article   = ArticleService.get(source[article]) ?: error("article not found on comment retrieve from db"),
        commenter = UserService.get(source[commenter])  ?: error("user not found on comment retrieve from db")
    )

}