@file:Suppress("DuplicatedCode")

package blogify.backend.routes.articles

import blogify.backend.database.Articles
import blogify.backend.database.Users
import blogify.backend.resources.Article
import blogify.backend.resources.models.eqr
import blogify.backend.resources.search.Search
import blogify.backend.resources.slicing.sanitize
import blogify.backend.resources.slicing.slice
import blogify.backend.routes.handling.*
import blogify.backend.services.UserService
import blogify.backend.services.articles.ArticleService
import blogify.backend.services.models.Service

import io.ktor.application.call
import io.ktor.routing.*
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.delete
import io.ktor.client.request.url
import io.ktor.client.features.json.JsonFeature
import io.ktor.content.TextContent
import io.ktor.http.ContentType
import io.ktor.response.respond

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

fun Route.articles() {

    route("/articles") {

        get("/") {
            fetchAll(ArticleService::getAll)
        }

        get("/{uuid}") {
            fetchWithId(ArticleService::get)
        }

        get("/forUser/{username}") {

            val params = call.parameters
            val username = params["username"] ?: error("Username is null")
            val selectedPropertyNames = params["fields"]?.split(",")?.toSet()

            UserService.getMatching { Users.username eq username }.fold(
                success = {
                    ArticleService.getMatching { Articles.createdBy eq it.single().uuid }.fold(
                        success = { articles ->
                            try {
                                selectedPropertyNames?.let { props ->

                                    call.respond(articles.map { it.slice(props) })

                                } ?: call.respond(articles.map { it.sanitize() })
                            } catch (bruhMoment: Service.Exception) {
                                call.respondExceptionMessage(bruhMoment)
                            }
                        },
                        failure = { call.respondExceptionMessage(it) }
                    )
                },
                failure = { call.respondExceptionMessage(it) }
            )
        }

        delete("/{uuid}") {
            deleteWithId(
                fetch = ArticleService::get,
                delete = ArticleService::delete,
                authPredicate = { user, article -> article.createdBy == user },
                doAfter = { id ->
                    HttpClient().use { client ->
                        client.delete<String> {
                            url("http://es:9200/articles/_doc/$id")
                        }.also { println(it) }
                    }
                    // DELETE /<index>/_doc/<_id>
                }
            )
        }

        patch("/{uuid}") {
            updateWithId (
                update = ArticleService::update,
                fetch = ArticleService::get,
                authPredicate = { user, article -> article.createdBy eqr user }
            )
        }

        post("/") {
            createWithResource(
                ArticleService::add,
                authPredicate = { user, article -> article.createdBy eqr user },
                doAfter = { article ->
                    HttpClient().use { client ->
                        val objectMapper = jacksonObjectMapper()
                        val jsonAsString = objectMapper.writeValueAsString(article)
                        println(jsonAsString)
                        client.post<String> {
                            url("http://es:9200/articles/_create/${article.uuid}")
                            body = TextContent(jsonAsString, contentType = ContentType.Application.Json)
                        }.also { println(it) }
                    }
                }
            )
        }

        get("/search") {
            val params = call.parameters
            val selectedPropertyNames = params["fields"]?.split(",")?.toSet()
            params["q"]?.let { query ->
                HttpClient() { install(JsonFeature) }.use { client ->
                    val parsed = client.get<Search<Article>>("http://es:9200/articles/_search?q=$query")
                    val hits = parsed.hits.hits.map { l -> l._source }
                    try {
                        selectedPropertyNames?.let { props ->

                            call.respond(hits.map { it.slice(props) })

                        } ?: call.respond(hits.map { it.sanitize() })
                    } catch (bruhMoment: Service.Exception) {
                        call.respondExceptionMessage(bruhMoment)
                    }
                    println("hits")
                    hits.forEach { println(it) }
                    call.respond(hits)
                }
            }
        }

        get("_validations") {
            getValidations<Article>()
        }

        articleComments()

    }

}
