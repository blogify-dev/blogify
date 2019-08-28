package blgoify.backend.routes.articles

import blgoify.backend.resources.Comment
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
            handleIdentifiedResourceFetchAll(CommentService::getForArticle)
        }

        post {
            handleResourceCreation(CommentService::add)
        }

        delete("/{uuid}") {
            handleResourceDeletion<Comment>(CommentService::remove)
        }

    }

}