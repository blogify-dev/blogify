package blgoify.backend.services.articles

import blgoify.backend.resources.Comment
import blgoify.backend.services.models.Service
import blgoify.backend.database.handling.query
import blgoify.backend.database.Comments
import blgoify.backend.database.Comments.uuid
import blgoify.backend.services.handling.handleResourceDBDelete
import blgoify.backend.services.handling.handleResourceDBFetch
import blgoify.backend.services.handling.handleResourceDBFetchAll
import blgoify.backend.services.models.ResourceResult
import blgoify.backend.services.models.ResourceResultSet

import com.github.kittinunf.result.coroutines.mapError

import org.jetbrains.exposed.sql.insert

import java.util.UUID

object CommentService : Service<Comment> {

    override suspend fun getAll(): ResourceResultSet<Comment> = handleResourceDBFetchAll(Comments)

    override suspend fun get(id: UUID): ResourceResult<Comment> = handleResourceDBFetch(Comments, uuid, id)

    override suspend fun add(res: Comment) = query {
        Comments.insert {
            it[uuid]          = res.uuid
            it[commenter]     = res.commenter.uuid
            it[article]       = res.article.uuid
            it[content]       = res.content
            it[parentComment] = res.parentComment?.uuid
        }

        return@query res
    }.mapError { e -> Service.Exception.Creating(e) }

    override suspend fun delete(id: UUID) = handleResourceDBDelete(Comments, uuid, id)

    override suspend fun update(res: Comment): ResourceResult<Comment> {
        TODO("not implemented !")
    }

}