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
            val params = call.parameters
            val length = params["amount"]?.toInt() ?: 25
            val requiredParamsToReturn = params["fields"]?.split(",") ?: error("Field parameters is null")
            ArticleService.getAll().fold(
                success = {
                    val returnList = mutableListOf<Map<String, Any>>()

                    it.take(length).forEach { article ->

                        val mapToReturn = mutableMapOf<String, Any>()

                        requiredParamsToReturn.forEach { property ->
                            mapToReturn[property] = getViaReflection<Any>(article, property)
                        }

                        returnList.add(mapToReturn)
                    }

                    call.respond(returnList)
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

        articleContent()

        articleCategories()

        articleComments()

    }

}