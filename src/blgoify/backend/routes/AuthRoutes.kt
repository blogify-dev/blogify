package blgoify.backend.routes

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route

sealed class UsernamePassword
data class UsernamePasswordCredentials(val username: String, val password: String) : UsernamePassword()

fun Route.auth() {
    route("/auth") {
        post("/signin") {
            val user = call.receive<UsernamePasswordCredentials>()
            val receivedUser = UserService.getByUsername(user.username)
            receivedUser?.let {
                if (it.username == user.username && it.password == user.password) {
                    call.respond(HttpStatusCode.Accepted)
                } else {
                    println(it)
                    call.respond(HttpStatusCode.BadRequest)
                }
            } ?: call.respond(HttpStatusCode.BadRequest)
        }

        post("/signup") {
            val user = call.receive<User>()
            if (UserService.add(user)) {
                call.respond(HttpStatusCode.Created)
            } else {
                call.respond(HttpStatusCode.InternalServerError)
            }
        }
    }
}
