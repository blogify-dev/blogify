package blogify.backend.database

import blogify.backend.resources.Article
import blogify.backend.resources.Comment
import blogify.backend.resources.User
import blogify.backend.resources.models.Resource
import blogify.backend.services.articles.ArticleService
import blogify.backend.services.UserService
import blogify.backend.services.articles.CommentService
import blogify.backend.services.models.Service

import com.github.kittinunf.result.coroutines.SuspendableResult

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

abstract class ResourceTable<R : Resource> : Table() {

    abstract suspend fun convert(source: ResultRow): SuspendableResult<R, Service.Exception.Fetching>

}

object Articles : ResourceTable<Article>() {

    val uuid       = uuid    ("uuid").primaryKey()
    val title      = varchar ("title", 512)
    val createdAt  = long    ("created_at")
    val createdBy  = uuid    ("created_by").references(Users.uuid, onDelete = ReferenceOption.SET_NULL)

    override suspend fun convert(source: ResultRow) = SuspendableResult.of<Article, Service.Exception.Fetching> { Article (
        uuid       = source[uuid],
        title      = source[title],
        createdAt  = source[createdAt],
        createdBy  = UserService.get(source[createdBy]).get(),
        content    = transaction {
            Content.select { Content.article eq source[uuid] }.singleOrNull()
        }?.let { Content.convert(it) } ?: error("no or multiple content in db for article ${source[uuid]}"),
        categories = transaction {
            Categories.select { Categories.article eq source[uuid] }.toList() }.map { Categories.convert(it) }
    ) }

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
    @Suppress("RemoveRedundantQualifierName")
    object Categories : Table() {

        val article = uuid("article").primaryKey().references(Articles.uuid, onDelete = ReferenceOption.CASCADE)
        val name    = varchar("name", 255).primaryKey()

        @Suppress("RedundantSuspendModifier")
        suspend fun convert(source: ResultRow) = Article.Category (
            name = source[name]
        )

    }

}

object Users : ResourceTable<User>() {

    val uuid     = uuid    ("uuid").primaryKey()
    val username = varchar ("username", 255)
    val password = varchar ("password", 255)

    init {
        index(true, username)
    }

    override suspend fun convert(source: ResultRow) = SuspendableResult.of<User, Service.Exception.Fetching> { User (
        uuid     = source[uuid],
        info     = transaction {
            UserInfo.select { UserInfo.user eq uuid }.singleOrNull()?. let { UserInfo.convert(it) } ?: error("")
        },
        username = source[username],
        password = source[password]
    ) }

    object UserInfo: Table() {

        val user = uuid("user").primaryKey().references(uuid, onDelete = ReferenceOption.CASCADE)
        val email = varchar("email", 255)
        val name = varchar("name", 255)

        fun convert(source: ResultRow) = User.PersonalInformation(
            name = source[name],
            email = source[email]
        )
    }

}

object Comments : ResourceTable<Comment>() {

    val uuid          = uuid ("uuid").primaryKey()
    val commenter     = uuid ("commenter").references(Users.uuid, onDelete = ReferenceOption.SET_NULL)
    val article       = uuid ("article").references(Articles.uuid, onDelete = ReferenceOption.NO_ACTION)
    val content       = text ("content")
    val parentComment = uuid ("parent_comment").references(uuid).nullable()

    override suspend fun convert(source: ResultRow) = SuspendableResult.of<Comment, Service.Exception.Fetching> { Comment (
        uuid          = source[uuid],
        content       = source[content],
        article       = ArticleService.get(source[article]).get(),
        commenter     = UserService.get(source[commenter]).get(),
        parentComment = source[parentComment]?.let { CommentService.get(it).get() }
    ) }

}