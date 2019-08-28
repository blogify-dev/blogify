package blgoify.backend.routes.articles

import blgoify.backend.database.Comments
import blgoify.backend.routes.handling.handleIdentifiedResourceFetchAll
import blgoify.backend.routes.handling.handleResourceCreation
import blgoify.backend.routes.handling.handleResourceDeletion
import blgoify.backend.routes.handling.handleResourceFetchAll
import blgoify.backend.services.articles.CommentService

import io.ktor.routing.*

fun Route.articleComments() {

    route("/comments") {

        get("/") {
            handleResourceFetchAll(CommentService::getAll)
        }

        get("/{uuid}") {
            handleIdentifiedResourceFetchAll(fetch = {
                CommentService.getMatching(Comments) { Comments.article eq it }
            })
        }

        post {
            handleResourceCreation(CommentService::add)
        }

        delete("/{uuid}") {
            handleResourceDeletion<Comment>(CommentService::remove)
        }

    }

}