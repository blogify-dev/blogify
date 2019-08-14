package me.benjozork.routes

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*
import me.benjozork.resources.Article
import me.benjozork.services.ArticleService

import java.util.*

fun Route.articles() {
    val service = ArticleService

    route("/articles") {

        post("/") {
            val receivedArticle = call.receive<Article>()

            if (service.add(receivedArticle)) {
                call.respond(HttpStatusCode.Created)
            } else {
                call.respond(HttpStatusCode.InternalServerError)
            }
        }

        get("/{uuid}") {
            val selectedUUID = call.parameters["uuid"]

            val selectedArticle = service.get(UUID.fromString(selectedUUID))

            if (selectedArticle == null)
                call.respond(HttpStatusCode.NotFound)
            else
                call.respond(selectedArticle)
        }

        get("/") {
            val result = service.getAll()
                .filter { it.createdAt >= (call.request.queryParameters["time"]?.toLong() ?: 0) }
                .filter { article -> call.request.queryParameters["title"]?.let { article.title == it } ?: true }
            call.respond(if (result.isNotEmpty()) result else HttpStatusCode.NotFound)
        }

        delete("/{uuid}") {
            val selectedUUID = call.parameters["uuid"]

            val selectedArticle = service.get(UUID.fromString(selectedUUID))

            if (selectedArticle == null)
                call.respond(HttpStatusCode.NotFound)
            else {
                service.remove(selectedArticle.uuid)
                call.respond(HttpStatusCode.OK)
            }
        }

        patch("/{uuid}") {
            val selectedUUID = call.parameters["uuid"]

            val selectedArticle = service.get(UUID.fromString(selectedUUID))
            val replacementArticle = call.receive<Article>()

            if (selectedArticle == null)
                call.respond(HttpStatusCode.NotFound)
            else {
                selectedArticle.copy(
                    title = replacementArticle.title
                )

                service.update(replacementArticle)
                call.respond(HttpStatusCode.OK)
            }
        }

    }

}