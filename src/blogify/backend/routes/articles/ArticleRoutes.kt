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
import kotlin.reflect.full.declaredMemberProperties

fun Route.articles() {

    route("/articles") {

        get("/") {
            val params = call.parameters
            println("Parameters:")
            println("params -> $params")
            println("params[field] -> ${params["fields"]}")
            println("params[field]::class.simpleName -> ${params["fields"]!!::class.simpleName}")
            val length = params["amount"]!!.toInt()
            val shitToGib = params["fields"]!!.split(",").also { it.forEach(::println) }
            println("--------------------------")

            val resource = ArticleService.getAll()

            println("Get via reflection:")
            val first = resource.get().first()
            println("Property access: ${first.title}")
            println("Reflection: ${getViaReflection<String>(first, "title")}")
            val map = mutableMapOf<String, Any>()
            shitToGib.forEach {
                map[it] = getViaReflection<Any>(first, it)
            }
            println(map)
            println("--------------------------")

            resource.fold(
                success = {
                    val articles = it.take(length)
                    val returnList = mutableListOf<Map<String, Any>>()
                    articles.forEach {article ->
                        val mapToReturn = mutableMapOf<String, Any>()
                        shitToGib.forEach {property ->
                            println("property $property")
                            println("article $article")
                            mapToReturn[property] = getViaReflection<Any>(article, property)
                        }
                        returnList.add(mapToReturn)
                        println("mapToReturn:")
                        println(mapToReturn)
                    }
                    println("returnList:")
                    println(returnList)
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