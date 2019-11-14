@file:Suppress("DuplicatedCode")

package blogify.backend.routes.articles

import blogify.backend.database.Articles
import blogify.backend.database.Comments
import blogify.backend.database.Users
import blogify.backend.resources.Article
import blogify.backend.resources.models.eqr
import blogify.backend.resources.search.Search
import blogify.backend.resources.search.asDocument
import blogify.backend.resources.reflect.sanitize
import blogify.backend.resources.reflect.slice
import blogify.backend.routes.handling.*
import blogify.backend.services.UserService
import blogify.backend.services.articles.ArticleService
import blogify.backend.services.articles.CommentService
import blogify.backend.services.models.Service
import blogify.backend.util.TYPESENSE_API_KEY

import io.ktor.application.call
import io.ktor.routing.*
import io.ktor.client.HttpClient
import io.ktor.client.features.json.JsonFeature
import io.ktor.content.TextContent
import io.ktor.http.ContentType
import io.ktor.response.respond
import io.ktor.client.request.*
import io.ktor.http.HttpStatusCode

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

fun Route.articles() {

    route("/articles") {

        get("/") {
            fetchAll(ArticleService::getAll)
        }

        get("/{uuid}") {
            fetchWithId(ArticleService::get)
        }

        get("/{uuid}/commentCount") {
            countReferringToResource(ArticleService::get, CommentService::getReferring, Comments.article)
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
                            url("http://ts:8108/collections/articles/documents/$id")
                            header("X-TYPESENSE-API-KEY", TYPESENSE_API_KEY)
                        }.also { println(it) }
                    }
                }
            )
        }

        patch("/{uuid}") {
            updateWithId (
                update = ArticleService::update,
                fetch = ArticleService::get,
                authPredicate = { user, article -> article.createdBy eqr user },
                doAfter = { replacement ->
                    HttpClient().use { client ->
                        client.delete<String> {
                            url("http://ts:8108/collections/articles/documents/${replacement.uuid}")
                            header("X-TYPESENSE-API-KEY", TYPESENSE_API_KEY)
                        }.also { println(it) }

                        val objectMapper = jacksonObjectMapper()
                        val jsonAsString = objectMapper.writeValueAsString(replacement.asDocument())
                        println(jsonAsString)
                        client.post<String> {
                            url("http://ts:8108/collections/articles/documents")
                            body = TextContent(jsonAsString, contentType = ContentType.Application.Json)
                            header("X-TYPESENSE-API-KEY", TYPESENSE_API_KEY)
                        }.also { println(it) }
                    }
                }
            )
        }

        post("/") {
            createWithResource(
                ArticleService::add,
                authPredicate = { user, article -> article.createdBy eqr user },
                doAfter = { article ->
                    HttpClient().use { client ->
                        val objectMapper = jacksonObjectMapper()
                        val jsonAsString = objectMapper.writeValueAsString(article.asDocument())
                        println(jsonAsString)
                        client.post<String> {
                            url("http://ts:8108/collections/articles/documents")
                            body = TextContent(jsonAsString, contentType = ContentType.Application.Json)
                            header("X-TYPESENSE-API-KEY", TYPESENSE_API_KEY)
                        }.also { println(it) }
                    }
                }
            )
        }

        get("/search") {
            val params = call.parameters
            val selectedPropertyNames = params["fields"]?.split(",")?.toSet()
            params["q"]?.let { query ->
                HttpClient { install(JsonFeature) }.use { client ->
                    val parsed = client.get<Search<Search.ArticleDocument>>("http://ts:8108/collections/articles/documents/search?q=$query&query_by=content,title")
                    parsed.hits?.let { hits -> // Some hits
                        val hitResources = hits.map { it.document.article() }
                        try {
                            selectedPropertyNames?.let { props ->

                                call.respond(hitResources.map { it.slice(props) })

                            } ?: call.respond(hitResources.map { it.sanitize() })
                        } catch (bruhMoment: Service.Exception) {
                            call.respondExceptionMessage(bruhMoment)
                        }
                    } ?: call.respond(HttpStatusCode.NoContent) // No hits
                }
            }
        }

        get("_validations") {
            getValidations<Article>()
        }

        articleComments()

    }

}
