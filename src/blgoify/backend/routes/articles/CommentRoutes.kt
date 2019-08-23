package blgoify.backend.routes.articles

import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route

import blgoify.backend.routes.handling.handleIdentifiedResourceFetchAll
import blgoify.backend.routes.handling.handleResourceCreation
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
            handleResourceCreation(CommentService::add)
        }

    }

}