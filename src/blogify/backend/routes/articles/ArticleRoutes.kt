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
import blogify.backend.services.models.Service
import blogify.backend.util.toUUID

fun Route.articles() {

    route("/articles") {

        get("/") {
            val params = call.parameters
            val length = params["amount"]?.toInt() ?: 25
            val requiredParamsToReturn = params["fields"]?.split(",")?.toSet()
            ArticleService.getAll().fold(
                success = { articles ->
                    try {
                        requiredParamsToReturn?.let {

                            call.respond(getMapFromParams(articles, length, it))

                        } ?: call.respond(articles.take(length))
                    } catch (bruhMoment: Service.Exception) {
                        call.respondExceptionMessage(bruhMoment)
                    }
                },
                failure = call::respondExceptionMessage
            )
        }

        get("/{uuid}") {
            fetchWithIdAndRespond(ArticleService::get)
        }

        get("/forUser/{uuid}") {
            handleIdentifiedResourceFetchAll(fetch = { userId ->
                ArticleService.getMatching(Articles) {
                    Articles.createdBy eq userId
                }
            })
        }

        delete("/{uuid}") {
            deleteWithId(ArticleService::get, ArticleService::delete, authPredicate = { user, article -> article.createdBy == user})
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

        post("/") {
            createWithResource(ArticleService::add, authPredicate = { user, article -> article.createdBy == user })
        }


        articleCategories()

        articleComments()

    }

}