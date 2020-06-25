package blogify.backend.routing.handling

import blogify.common.util.assertGet
import blogify.backend.annotations.BlogifyDsl
import blogify.backend.auth.handling.authenticated
import blogify.database.handling.query
import blogify.backend.database.tables.Comments
import blogify.backend.pipelines.queryUuid
import blogify.backend.pipelines.wrapping.RequestContextFunction
import blogify.backend.util.reason

import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert

import io.ktor.http.HttpStatusCode
import io.ktor.response.respond

@ExperimentalStdlibApi
private val likes = Comments.Likes

/**
 * Request handler for fetching comment like status
 */
@BlogifyDsl
@ExperimentalStdlibApi
val getCommentLikeStatus: RequestContextFunction<Unit> = {
    val id by queryUuid

    authenticated { user ->
        val liked = query {
            likes.select {
                (likes.comment eq id) and (likes.user eq user.uuid)
            }.count()
        }.assertGet() == 1L

        call.respond(liked)
    }
}

/**
 * Request handler for flipping comment like status
 */
@BlogifyDsl
@ExperimentalStdlibApi
val flipCommentLike: RequestContextFunction<Unit> = {
    val id by queryUuid

    authenticated { user ->
        // Figure whether the comment was already liked by the user
        val alreadyLiked = query {
            likes.select {
                (likes.comment eq id) and (likes.user eq user.uuid)
            }.count()
        }.assertGet() == 1L

        if (!alreadyLiked) { // Add a like if none were present
            query {
                likes.insert {
                    it[likes.comment] = id
                    it[likes.user] = user.uuid
                }
            }.assertGet()

            call.respond(HttpStatusCode.OK, reason("comment liked"))
        } else { // Remove an existing like if there was one
            query {
                likes.deleteWhere {
                    (likes.comment eq id) and (likes.user eq user.uuid)
                }
            }.assertGet()

            call.respond(HttpStatusCode.OK, reason("comment unliked"))
        }
    }
}
