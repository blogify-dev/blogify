package blogify.backend.routes.admin

import io.ktor.routing.Route
import io.ktor.routing.route

fun Route.admin() {
    route("/admin") {
        adminArticles()
        adminUsers()
        adminComments()
        adminSearch()
    }
}