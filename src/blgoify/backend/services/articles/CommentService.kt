package blgoify.backend.services.articles

import blgoify.backend.resources.Comment
import blgoify.backend.services.models.Service
import blgoify.backend.util.query
import blgoify.backend.database.Comments.convert
import blgoify.backend.database.Articles
import blgoify.backend.database.Comments
import blgoify.backend.util.booleanReturnQuery

import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll

import java.util.UUID

object CommentService : Service<Comment> {

    override suspend fun getAll(): Set<Comment> = query {
        Comments.selectAll().toSet()
    }.map { convert(it) }.toSet()

    override suspend fun get(id: UUID): Comment? = query {
        Comments.select { Articles.uuid eq id }.map { convert(it) }.single()
    }

    override suspend fun add(res: Comment) = booleanReturnQuery {
        Comments.insert {
            it[uuid]      = res.uuid;
            it[commenter] = res.commenter.uuid;
            it[article]   = res.article.uuid;
            it[content]   = res.content;
        }.run { Unit }
    }

    override suspend fun remove(id: UUID): Boolean {
        return true
    }

    override suspend fun update(res: Comment): Boolean {
        return true
    }

    suspend fun getForArticle(article: UUID): List<Comment> = query {
        Comments.select { Comments.article eq article }.map { convert(it) }
    }

    suspend fun getByUser(user: UUID): List<Comment> = query {
        Comments.select { Comments.commenter eq user }.map { convert(it) }
    }

}