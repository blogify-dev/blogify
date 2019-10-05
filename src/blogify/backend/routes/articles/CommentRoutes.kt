package blogify.backend.routes.articles

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*

import blogify.backend.database.Comments
import blogify.backend.resources.Comment
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
            deleteWithId(CommentService::get, CommentService::delete)
        }

        patch("/{uuid}") {
            val selectedUUID = call.parameters["uuid"]

            val replacementComment = call.receive<Comment>()
            val selectedComment = selectedUUID?.toUUID()?.let { CommentService.get(call, it) }

            if (selectedComment == null)
                call.respond(HttpStatusCode.NotFound)
            else {
                CommentService.update(replacementComment)
                call.respond(HttpStatusCode.OK)
            }
        }

        post("/") {
            createWithResource(CommentService::add)
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
