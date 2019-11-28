package blogify.backend.routes.admin

import blogify.backend.auth.handling.runAuthenticated
import blogify.backend.resources.Article
import blogify.backend.search.Typesense

import io.ktor.application.call
import io.ktor.client.call.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route

fun Route.adminSearch() {
    route("/search") {
        post("/reindex") {
            runAuthenticated(predicate = { it.isAdmin }) {
                Typesense.refreshIndex<Article>().let {
                    call.respond(mapOf("ts_response" to it.receive<Map<String, Any?>>()))
                }
            }
        }
    }
}