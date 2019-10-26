package blogify.backend.routes.articles

import io.ktor.application.call
import io.ktor.routing.*

import blogify.backend.database.Articles
import blogify.backend.resources.Article
import blogify.backend.routes.handling.*
import blogify.backend.services.articles.ArticleService

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
            deleteWithId(ArticleService::get, ArticleService::delete, authPredicate = { user, article -> article.createdBy.uuid == user.uuid })
        }

        patch("/{uuid}") {
            updateWithId (
                update = ArticleService::update,
                fetch = ArticleService::get,
                authPredicate = { user, article -> article.createdBy.uuid == user.uuid }
            )
        }

        post("/") {
            createWithResource(ArticleService::add, authPredicate = { user, article -> article.createdBy.uuid == user.uuid })
        }

        articleComments()

    }

}
