package blgoify.backend.routes.articles

import blgoify.backend.routes.handling.handleResourceFetch
import blgoify.backend.services.articles.ArticleService
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route

fun Route.articleCategory() {

    route("/category/") {

        get("/{uuid}") {
            handleResourceFetch(ArticleService::get, transform = { it.category() })
        }

    }

}