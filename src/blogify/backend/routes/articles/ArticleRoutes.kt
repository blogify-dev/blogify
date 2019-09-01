package blogify.backend.routes.articles

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*

import blogify.backend.resources.Article
import blogify.backend.auth.handling.authenticatedBy
import blogify.backend.auth.handling.isUser
import blogify.backend.routes.handling.*
import blogify.backend.services.articles.ArticleService
import blogify.backend.services.UserService
import blogify.backend.util.toUUID

fun Route.articles() {

    route("/articles") {

        get("/") {
            handleResourceFetchAll(ArticleService::getAll)
        }

        get("/{uuid}") {
            handleResourceFetch(ArticleService::get)
        }

        delete("/{uuid}") {
            handleResourceDeletion(ArticleService::delete)
        }

        patch("/{uuid}") {
            val selectedUUID = call.parameters["uuid"]

            val selectedArticle    = selectedUUID?.toUUID()?.let { ArticleService.get(it) }
            val replacementArticle = call.receive<Article>()

            if (selectedArticle == null)
                call.respond(HttpStatusCode.NotFound)
            else {
                call.respond(ArticleService.update(replacementArticle))
            }
        }

        post("/") { authenticatedBy(predicate = isUser(UserService.getAll().get().toList()[0])) {
            handleResourceCreation(ArticleService::add)
        }}

        articleContent()

        articleCategory()

        articleComments()

    }

}