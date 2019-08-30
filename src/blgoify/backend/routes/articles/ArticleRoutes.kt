package blgoify.backend.routes.articles

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*

import blgoify.backend.resources.Article
import blgoify.backend.auth.predicates.authenticatedBy
import blgoify.backend.auth.predicates.isUser
import blgoify.backend.routes.handling.*
import blgoify.backend.services.articles.ArticleService
import blgoify.backend.services.UserService
import blgoify.backend.util.toUUID

fun Route.articles() {

    route("/articles") {

        get("/") {
            handleResourceFetchAll(ArticleService::getAll)
        }

        get("/{uuid}") {
            handleResourceFetch(ArticleService::get)
        }

        post("/") { authenticatedBy(predicate = isUser(UserService.getAll().toList()[0])) {
            handleResourceCreation(ArticleService::add)
        }}

        delete("/{uuid}") {
            handleResourceDeletion(ArticleService::remove)
        }

        patch("/{uuid}") {
            val selectedUUID = call.parameters["uuid"]

            val selectedArticle    = selectedUUID?.toUUID()?.let { ArticleService.get(it) }
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