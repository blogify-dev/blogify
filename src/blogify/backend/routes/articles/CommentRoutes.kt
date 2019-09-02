package blogify.backend.routes.articles

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond

import blogify.backend.database.Comments
import blogify.backend.resources.Comment
import blogify.backend.routes.handling.handleIdentifiedResourceFetchAll
import blogify.backend.routes.handling.handleResourceCreation
import blogify.backend.routes.handling.handleResourceDeletion
import blogify.backend.routes.handling.handleResourceFetchAll
import blogify.backend.routes.handling.respondExceptionMessage
import blogify.backend.services.articles.CommentService
import blogify.backend.util.toUUID

import io.ktor.routing.*
import org.jetbrains.exposed.sql.and

fun Route.articleComments() {

    route("/comments") {

        get("/") {
            handleResourceFetchAll(CommentService::getAll)
        }

        get("/{uuid}") {
            handleIdentifiedResourceFetchAll(fetch = { articleId ->
                CommentService.getMatching(Comments) { Comments.article eq articleId and Comments.parentComment.isNull() }
            })
        }

        delete("/{uuid}") {
            handleResourceDeletion(CommentService::delete)
        }

        patch("/{uuid}") {
            val selectedUUID = call.parameters["uuid"]

            val replacementComment = call.receive<Comment>()
            val selectedComment = selectedUUID?.toUUID()?.let { CommentService.get(it) }

            if (selectedComment == null)
                call.respond(HttpStatusCode.NotFound)
            else {
                CommentService.update(replacementComment)
                call.respond(HttpStatusCode.OK)
            }
        }

        post("/") {
            handleResourceCreation(CommentService::add)
        }

        get("/tree/{uuid}") {
            call.parameters["uuid"]?.toUUID()?.let { givenUUID ->
                CommentService.getMatching(Comments) { Comments.parentComment eq givenUUID }
                    .fold (
                        success = {
                            call.respond(it)
                        },
                        failure = { ex ->
                            call.respondExceptionMessage(ex)
                        }
                    )
            }
        }

    }

}