package blogify.devend.routes

import blogify.backend.pipelines.obtainResource
import blogify.backend.pipelines.optionalParam
import blogify.backend.pipelines.queryUuid
import blogify.backend.pipelines.requestContext
import blogify.backend.resources.Comment
import blogify.devend.utils.comment
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route
import java.util.*

fun Route.commentsSeedRoutes() {
    route("/comment") {
        post("/article/{uuid}") {
            requestContext {
                val article by queryUuid
                val amount = optionalParam("amount")?.toInt() ?: 1


                val created = mutableListOf<Comment>()
                (1..amount).forEach {
                    val toCreate = comment.copy(article = obtainResource(article), createdAt = comment.createdAt - it, uuid = UUID.randomUUID())
                    created.add(repository<Comment>().add(toCreate).get())
                }
                call.respond(HttpStatusCode.Created, object { val created = created })
            }
        }
    }
}
