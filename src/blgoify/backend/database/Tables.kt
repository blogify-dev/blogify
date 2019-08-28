package blgoify.backend.database

import blgoify.backend.resources.Article
import blgoify.backend.resources.Comment
import blgoify.backend.resources.User
import blgoify.backend.resources.models.Resource
import blgoify.backend.services.articles.ArticleService
import blgoify.backend.services.UserService
import blgoify.backend.services.articles.CommentService
import blgoify.backend.util.encodeToSet

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

abstract class ResourceTable<R : Resource> : Table() {

    abstract suspend fun convert(source: ResultRow): R

}

object Articles : ResourceTable<Article>() {

    val uuid      = uuid    ("uuid").primaryKey()
    val title     = varchar ("title", 512)
    val createdAt = long    ("created_at")
    val createdBy = uuid    ("created_by").references(Users.uuid, onDelete = ReferenceOption.SET_NULL)
    val categories = varchar("categories", 512)

    override suspend fun convert(source: ResultRow) = Article (
        uuid      = source[uuid],
        title     = source[title],
        createdAt = source[createdAt],
        createdBy = UserService.get(source[createdBy]) ?: error("no user in db for article ${source[uuid]}"),
        categories = source[categories].encodeToSet(),
        content   = transaction {
            Content.select { Content.article eq source[uuid] }.singleOrNull()
        }?.let { Content.convert(it) } ?: error("no or multiple content in db for article ${source[uuid]}")
    )

    @Suppress("RemoveRedundantQualifierName")
    object Content : Table() {

        val article = uuid ("article").primaryKey().references(Articles.uuid, onDelete = ReferenceOption.CASCADE)
        val text    = text ("text")
        val summary = text ("summary")

        @Suppress("RedundantSuspendModifier")
        suspend fun convert(source: ResultRow) = Article.Content (
            text    = source[text],
            summary = source[summary]
        )

    }

}

object Users : ResourceTable<User>() {

    val uuid =     uuid    ("uuid").primaryKey()
    val name =     varchar ("name", 255)
    val username = varchar ("username", 255)
    val password = varchar ("password", 255)

    init {
        index(true, username)
    }

    override suspend fun convert(source: ResultRow) = User (
        uuid     = source[uuid],
        name     = source[name],
        username = source[username],
        password = source[password]
    )

}

object Comments : ResourceTable<Comment>() {

    val uuid      = uuid ("uuid").primaryKey()
    val commenter = uuid ("commenter").references(Users.uuid, onDelete = ReferenceOption.SET_NULL)
    val article   = uuid ("article").references(Articles.uuid, onDelete = ReferenceOption.NO_ACTION)
    val content   = text ("content")
    val parentComment = uuid("parent_comment").nullable()

    override suspend fun convert(source: ResultRow) = Comment (
        uuid          = source[uuid],
        content       = source[content],
        article       = ArticleService.get(source[article]) ?: error("article not found on comment retrieve from db"),
        commenter     = UserService.get(source[commenter])  ?: error("user not found on comment retrieve from db"),
        parentComment = source[parentComment]?.let { CommentService.get(it) }
    )

}