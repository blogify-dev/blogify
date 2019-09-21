package blogify.backend.routes.articles

import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route

import blogify.backend.resources.Article
import blogify.backend.routes.handling.fetchWithIdAndRespond
import blogify.backend.services.articles.ArticleService

fun Route.articleContent() {

    route("/content/") {

        get("/{uuid}") {
            fetchWithIdAndRespond(fetch = ArticleService::get, transform = Article::content)
        }

    }

}