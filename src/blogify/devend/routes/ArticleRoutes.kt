package blogify.devend.routes

import blogify.backend.pipelines.param
import blogify.backend.pipelines.requestContext
import blogify.backend.resources.Article
import blogify.backend.resources.user.User
import blogify.devend.utils.article
import blogify.devend.utils.user
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route
import java.util.*


fun Route.articleSeedRoutes() {
    route("/article") {
        post {
            requestContext {
                val created = mutableListOf<Article>()
                repository<User>().add(user).fold(success = {}, failure = {})
                (1..param("amount").toInt()).forEach {
                    created.add(repository<Article>().add(article.copy(
                        title = "${article.title} $it",
                        content = "${article.content} $it",
                        summary = "${article.summary} $it",
                        createdAt = article.createdAt - it,
                        uuid = UUID.randomUUID()
                    )).get())
                }
                call.respond(HttpStatusCode.Created, object { val created = created })
            }
        }
    }
}
