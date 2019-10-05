package blogify.backend.routes.articles

import blogify.backend.database.Articles
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*

import blogify.backend.resources.Article
import blogify.backend.routes.handling.*
import blogify.backend.services.articles.ArticleService
import blogify.backend.util.toUUID

fun Route.articles() {

    route("/articles") {

        get("/") {
            fetchAll(ArticleService::getAll)
        }

        get("/{uuid}") {
            fetchWithId(ArticleService::get)
        }

        get("/forUser/{uuid}") {
            fetchAllWithId(fetch = { userId ->
                ArticleService.getMatching(call) {
                    Articles.createdBy eq userId
                }
            })
        }

        delete("/{uuid}") {
            deleteWithId(ArticleService::get, ArticleService::delete, authPredicate = { user, article -> article.createdBy == user})
        }

        patch("/{uuid}") {
            updateWithId(
                update = ArticleService::update,
                fetch = ArticleService::get,
                authPredicate = { user, article -> article.createdBy == user }
            )
        }

        post("/") {
            createWithResource(ArticleService::add, authPredicate = { user, article -> article.createdBy == user })
        }

        articleComments()

    }

}
