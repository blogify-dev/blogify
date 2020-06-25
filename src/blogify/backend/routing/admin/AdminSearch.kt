package blogify.backend.routing.admin

import blogify.backend.auth.handling.authenticated
import blogify.backend.pipelines.wrapping.ApplicationContext
import blogify.backend.resources.Article
import blogify.backend.resources.user.User
import blogify.backend.pipelines.requestContext
import blogify.backend.search.Typesense
import reflectify.util.Dto

import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route

@ExperimentalStdlibApi
fun Route.makeAdminRoutes() {

    route("/admin/search") {

        get("/reindex") {

            requestContext {

                val what = call.parameters["what"] ?: error("bruh")

                authenticated({ it.isAdmin }) {
                    when(what) {
                        "article" -> Typesense.refreshIndex<Article>(this@requestContext)
                        "user" -> Typesense.refreshIndex<User>(this@requestContext)
                        else -> error("Wrong param provided")
                    }.let {
                        call.respond(mapOf("ts_response" to it.receive<Dto>()))
                    }
                }

            }

        }

    }

}
