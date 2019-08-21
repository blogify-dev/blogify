package blgoify.backend.routes.articles

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*

import blgoify.backend.resources.Article
import blgoify.backend.routes.handling.handleResourceFetchAll
import blgoify.backend.services.articles.ArticleService
import blgoify.backend.routes.handling.handleResourceFetch
import blgoify.backend.util.toUUID

fun Route.articles() {

    route("/articles") {

        get("/") {
            handleResourceFetchAll(ArticleService::getAll)
        }

        get("/{uuid}") {
            handleResourceFetch(ArticleService::get)
        }

        post("/") {
            val receivedArticle = call.receive<Article>()

            ArticleService.add(receivedArticle) // Temporary

            call.respond(HttpStatusCode.Created)
        }

        delete("/{uuid}") {
            val selectedUUID = call.parameters["uuid"]

            val selectedArticle = selectedUUID?.toUUID()?.let { ArticleService.get(it) }

            if (selectedArticle == null)
                call.respond(HttpStatusCode.NotFound)
            else {
                ArticleService.remove(selectedArticle.uuid)
                call.respond(HttpStatusCode.OK)
            }
        }

        patch("/{uuid}") {
            val selectedUUID = call.parameters["uuid"]

            val selectedArticle = selectedUUID?.toUUID()?.let { ArticleService.get(it) }
            val replacementArticle = call.receive<Article>()

            if (selectedArticle == null)
                call.respond(HttpStatusCode.NotFound)
            else {
                ArticleService.update(replacementArticle)
                call.respond(HttpStatusCode.OK)
            }
        }

        articleContent()

        articleComments()

    }

}