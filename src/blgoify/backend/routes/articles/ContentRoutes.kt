package blgoify.backend.routes.articles

import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route

import blgoify.backend.routes.handling.handleSimpleResourceFetch
import blgoify.backend.services.articles.ArticleService

import java.util.UUID

fun Route.articleContent() {

    route("/content/") {

        val service = ArticleService

        get("/{uuid}") {
            handleSimpleResourceFetch(ArticleService::get, transform = { it.content })
        }

    }

}