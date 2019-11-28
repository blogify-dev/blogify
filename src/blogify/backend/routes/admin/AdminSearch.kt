package blogify.backend.routes.admin

import blogify.backend.resources.Article
import blogify.backend.search.Typesense
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route

fun Route.adminSearch() {
    route("/search") {
        post("/reindex") {
            Typesense.refreshIndex<Article>().let {
                call.respond(if (it) HttpStatusCode.Created else HttpStatusCode.InternalServerError)
            }
        }
    }
}