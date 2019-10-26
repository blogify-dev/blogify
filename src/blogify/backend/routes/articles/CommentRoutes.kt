package blogify.backend.routes.articles

import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.*

import blogify.backend.database.Comments
import blogify.backend.routes.handling.*
import blogify.backend.services.articles.CommentService
import blogify.backend.util.expandCommentNode
import blogify.backend.util.toUUID

import org.jetbrains.exposed.sql.and

fun Route.articleComments() {

    route("/comments") {

        get("/") {
            fetchAll(CommentService::getAll)
        }

        get("/{uuid}") {
            fetchAllWithId(fetch = { articleId ->
                CommentService.getMatching(call) { Comments.article eq articleId and Comments.parentComment.isNull() }
            })
        }

        delete("/{uuid}") {
            deleteWithId(CommentService::get, CommentService::delete, authPredicate = { user, comment -> comment.commenter eqr user })
        }

        patch("/{uuid}") {
            updateWithId (
                update = CommentService::update,
                fetch = CommentService::get,
                authPredicate = { user, comment -> comment.commenter eqr user }
            )
        }

        post("/") {
            createWithResource(CommentService::add, authPredicate = { user, comment -> comment.commenter eqr user })
        }

        get("/tree/{uuid}") {
            val fetched = CommentService.get(call, call.parameters["uuid"]!!.toUUID())

            val depth = call.parameters["depth"]?.toInt() ?: 5

            call.respond(expandCommentNode(call, fetched.get(), depth = depth))
        }

    }

}
