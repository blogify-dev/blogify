package blogify.backend.services.articles

import blogify.backend.resources.Comment
import blogify.backend.services.models.Service
import blogify.backend.database.handling.query
import blogify.backend.database.Comments
import blogify.backend.database.Comments.uuid

import com.github.kittinunf.result.coroutines.mapError

import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.update

object CommentService : Service<Comment>(table = Comments) {

    override suspend fun add(res: Comment) = query {
        Comments.insert {
            it[uuid]          = res.uuid
            it[commenter]     = res.commenter.uuid
            it[article]       = res.article.uuid
            it[content]       = res.content
            it[parentComment] = res.parentComment?.uuid
        }

        return@query res
    }.mapError { e -> Exception.Creating(e) }

    override suspend fun update(res: Comment) = query {
        Comments.update({ uuid eq res.uuid }) {
            it[content] = res.content
        }

        return@query res
    }.mapError { e -> Exception.Updating(e) }

}
