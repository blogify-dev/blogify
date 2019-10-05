package blogify.backend.routes.articles

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.*

import blogify.backend.database.Comments
import blogify.backend.routes.handling.*
import blogify.backend.services.articles.CommentService
import blogify.backend.util.toUUID

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper

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
            deleteWithId(CommentService::get, CommentService::delete, authPredicate = { user, comment -> comment.commenter == user })
        }

        patch("/{uuid}") {
            updateWithId(
                update = CommentService::update,
                fetch = CommentService::get,
                authPredicate = { user, comment -> comment.commenter == user }
            )
        }

        post("/") {
            createWithResource(CommentService::add, authPredicate = { user, comment -> comment.commenter == user })
        }

        get("/tree/{uuid}") {
            call.parameters["uuid"]?.toUUID()?.let { givenUUID ->

                val comments = CommentService.getMatching(call) { Comments.parentComment eq givenUUID }.get().map {
                     ObjectMapper().convertValue<Map<String, Any>>(it, object: TypeReference<Map<String, Any>>() {}).toMutableMap().apply {
                         this["children"] = CommentService.getMatching(call) { Comments.parentComment eq this@apply["uuid"].toString().toUUID() }.get()
                     }
                }
                println(comments)

                call.respond(comments)

            } ?: call.respond(HttpStatusCode.BadRequest)
        }

    }

}
