package blgoify.backend.routes.articles

import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route

import blgoify.backend.routes.handling.handleResourceFetch
import blgoify.backend.services.articles.ArticleService

fun Route.articleContent() {

    route("/content/") {

        get("/{uuid}") {
            handleResourceFetch(fetch = { ArticleService.get(it) }, transform = { it.content() })
        }

    }

}