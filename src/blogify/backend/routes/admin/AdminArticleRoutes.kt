@file:Suppress("DuplicatedCode")

package blogify.backend.routes.admin

import blogify.backend.resources.search.asDocument
import blogify.backend.routes.handling.deleteWithId
import blogify.backend.routes.handling.updateWithId
import blogify.backend.services.articles.ArticleService
import blogify.backend.util.TYPESENSE_API_KEY
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.content.TextContent
import io.ktor.routing.Route
import io.ktor.routing.delete
import io.ktor.routing.patch
import io.ktor.routing.route

fun Route.adminArticles() {
    route("/articles") {

        delete("/{uuid}") {
            deleteWithId(
                fetch = ArticleService::get,
                delete = ArticleService::delete,
                authPredicate = { user, _ -> user.isAdmin },
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
                authPredicate = { user, _ -> user.isAdmin },
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
    }
}



