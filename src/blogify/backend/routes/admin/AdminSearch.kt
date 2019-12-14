package blogify.backend.routes.admin

import blogify.backend.auth.handling.runAuthenticated
import blogify.backend.resources.Article
import blogify.backend.resources.User
import blogify.backend.search.Typesense

import io.ktor.application.call
import io.ktor.client.call.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route

fun Route.adminSearch() {

    route("/admin/search") {

        get("/reindex") {
            val what = call.parameters["what"] ?: error("bruh")
            runAuthenticated(predicate = { it.isAdmin }) {
                when(what) {
                    "article" -> Typesense.refreshIndex<Article>()
                    "user" -> Typesense.refreshIndex<User>()
                    else -> error("Wrong param provided")
                }.let {
                    call.respond(mapOf("ts_response" to it.receive<Map<String, Any?>>()))
                }
            }
        }

    }

}