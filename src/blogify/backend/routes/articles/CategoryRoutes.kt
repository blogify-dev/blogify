package blogify.backend.routes.articles

import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route

import blogify.backend.resources.Article
import blogify.backend.routes.handling.handleResourceFetch
import blogify.backend.services.articles.ArticleService

fun Route.articleCategory() {

    route("/category/") {

        get("/{uuid}") {
            handleResourceFetch(fetch = ArticleService::get, transform = Article::category )
        }

    }

}