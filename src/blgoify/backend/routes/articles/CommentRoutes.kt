package blgoify.backend.routes.articles

import io.ktor.routing.*

import blgoify.backend.routes.handling.handleIdentifiedResourceFetchAll
import blgoify.backend.routes.handling.handleResourceFetchAll
import blgoify.backend.services.articles.CommentService

fun Route.articleComments() {

    route("/comments") {

        get("/") {
            handleResourceFetchAll(CommentService::getAll)
        }

        get("/{uuid}") {
            handleIdentifiedResourceFetchAll(CommentService::getForArticle)
        }

        post {

        }

    }

}