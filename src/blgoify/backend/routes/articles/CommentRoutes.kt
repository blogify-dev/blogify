package blgoify.backend.routes.articles

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond

import blgoify.backend.database.Comments
import blgoify.backend.resources.Comment
import blgoify.backend.routes.handling.handleIdentifiedResourceFetchAll
import blgoify.backend.routes.handling.handleResourceCreation
import blgoify.backend.routes.handling.handleResourceDeletion
import blgoify.backend.routes.handling.handleResourceFetchAll
import blgoify.backend.services.articles.CommentService
import blgoify.backend.util.toUUID

import io.ktor.routing.*

fun Route.articleComments() {

    route("/comments") {

        get("/") {
            handleResourceFetchAll(CommentService::getAll)
        }

        get("/{uuid}") {
            handleIdentifiedResourceFetchAll(fetch = { articleId ->
                CommentService.getMatching(Comments) { Comments.article eq articleId }
            })
        }

        delete("/{uuid}") {
            handleResourceDeletion(CommentService::delete)
        }

        patch("/{uuid}") {
            val selectedUUID = call.parameters["uuid"]

            val replacementComment = call.receive<Comment>()
            val selectedComment    = selectedUUID?.toUUID()?.let { CommentService.get(it) }

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

    }

}