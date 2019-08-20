package blgoify.backend.routes.articles

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route

import blgoify.backend.services.articles.ArticleService

import java.util.UUID

fun Route.articleContent() {

    route("/content/") {

        val service = ArticleService

        get("/{uuid}") {
            call.parameters["uuid"]?.let {

                val uuid = UUID.fromString(it)

                service.get(uuid)?.let { article ->
                    call.respond(article.content)
                } ?: call.respond(HttpStatusCode.NotFound)

            } ?: call.respond(HttpStatusCode.BadRequest)
        }

    }

}