package blgoify.backend.routes.articles

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.*

import blgoify.backend.services.ArticleService
import blgoify.backend.services.CommentService
import blgoify.backend.services.UserService
import blgoify.backend.util.toUUID

fun Route.articleComments() {

    val articleService = ArticleService
    val userService    = UserService

    route("/comments") {

        get("/{uuid}") {
            call.parameters["uuid"]?.let {
                ArticleService.get(it.toUUID()!!)?.let { article ->
                    call.respond(CommentService.getForArticle(article))
                } ?: call.respond(HttpStatusCode.NotFound)
            } ?: call.respond(HttpStatusCode.BadRequest)
        }

    }

}