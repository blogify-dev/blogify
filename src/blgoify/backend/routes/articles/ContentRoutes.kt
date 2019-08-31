package blgoify.backend.routes.articles

import blgoify.backend.resources.Article
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route

import blgoify.backend.routes.handling.handleResourceFetch
import blgoify.backend.services.articles.ArticleService

fun Route.articleContent() {

    route("/content/") {

        get("/{uuid}") {
            handleResourceFetch(fetch = ArticleService::get, transform = Article::content)
        }

    }

}