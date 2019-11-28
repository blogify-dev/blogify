@file:Suppress("RemoveRedundantQualifierName")

package blogify.backend.database

import blogify.backend.resources.Article
import blogify.backend.resources.Comment
import blogify.backend.resources.User
import blogify.backend.resources.models.Resource
import blogify.backend.resources.static.models.StaticResourceHandle
import blogify.backend.services.articles.ArticleService
import blogify.backend.services.UserService
import blogify.backend.services.articles.CommentService
import blogify.backend.services.models.Service

import io.ktor.application.ApplicationCall
import io.ktor.http.ContentType

import org.jetbrains.exposed.sql.ReferenceOption.*
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

import com.github.kittinunf.result.coroutines.SuspendableResult

abstract class ResourceTable<R : Resource> : Table() {

    abstract suspend fun convert(callContext: ApplicationCall, source: ResultRow): SuspendableResult<R, Service.Exception.Fetching>

    val uuid = uuid("uuid").primaryKey()

}

object Articles : ResourceTable<Article>() {

    val title      = varchar ("title", 512)
    val createdAt  = integer    ("created_at")
    val createdBy  = uuid    ("created_by").references(Users.uuid, onDelete = SET_NULL)
    val content    = text    ("content")
    val summary    = text    ("summary")

    override suspend fun convert(callContext: ApplicationCall, source: ResultRow) = SuspendableResult.of<Article, Service.Exception.Fetching> {
        Article (
            uuid       = source[uuid],
            title      = source[title],
            createdAt  = source[createdAt],
            createdBy  = UserService.get(callContext, source[createdBy]).get(),
            content    = source[content],
            summary    = source[summary],
            categories = transaction {
                Categories.select { Categories.article eq source[uuid] }.toList()
            }.map { Categories.convert(it) }
        )
    }

    object Categories : Table() {

        val article = uuid("article").primaryKey().references(Articles.uuid, onDelete = CASCADE)
        val name    = varchar("name", 255).primaryKey()

        @Suppress("RedundantSuspendModifier")
        suspend fun convert(source: ResultRow) = Article.Category (
            name = source[name]
        )

    }

}

object Users : ResourceTable<User>() {

    val username       = varchar ("username", 255)
    val password       = varchar ("password", 255)
    val email          = varchar ("email", 255)
    val name           = varchar ("name", 255)
    val profilePicture = varchar ("profile_picture", 32).references(Uploadables.fileId, onDelete = SET_NULL, onUpdate = RESTRICT).nullable()
    val isAdmin        = bool("is_admin")

    init {
        index(true, username)
    }

    override suspend fun convert(callContext: ApplicationCall, source: ResultRow) = SuspendableResult.of<User, Service.Exception.Fetching> {
        User (
            uuid           = source[uuid],
            username       = source[username],
            password       = source[password],
            name           = source[name],
            email          = source[email],
            isAdmin        = source[isAdmin],
            profilePicture = source[profilePicture]?.let { transaction {
                Uploadables.select { Uploadables.fileId eq source[profilePicture]!! }.limit(1).single()
            }.let { Uploadables.convert(callContext, it).get() } } ?: StaticResourceHandle.None(ContentType.Any)
        )
    }

}

object Comments : ResourceTable<Comment>() {

    val commenter     = uuid ("commenter").references(Users.uuid, onDelete = SET_NULL)
    val article       = uuid ("article").references(Articles.uuid, onDelete = CASCADE)
    val content       = text ("content")
    val parentComment = uuid ("parent_comment").references(uuid, onDelete = CASCADE).nullable()

    override suspend fun convert(callContext: ApplicationCall, source: ResultRow) = SuspendableResult.of<Comment, Service.Exception.Fetching> {
        Comment (
            uuid          = source[uuid],
            content       = source[content],
            article       = ArticleService.get(callContext, source[article]).get(),
            commenter     = UserService.get(callContext, source[commenter]).get(),
            parentComment = source[parentComment]?.let { CommentService.get(callContext, it).get() }
        )
    }

}

object Uploadables : Table() {

    val fileId      = varchar ("id", 32).primaryKey()
    val contentType = varchar ("content_type", 64)

    suspend fun convert(@Suppress("UNUSED_PARAMETER") callContext: ApplicationCall, source: ResultRow) = SuspendableResult.of<StaticResourceHandle.Ok, Service.Exception> {
        StaticResourceHandle.Ok (
            contentType = ContentType.parse(source[contentType]),
            fileId      = source[fileId]
        )
    }

}
