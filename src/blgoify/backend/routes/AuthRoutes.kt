package blgoify.backend.routes

import blgoify.backend.resources.User
import blgoify.backend.services.UserService
import blgoify.backend.util.toUUID
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.util.*
import kotlin.concurrent.schedule

/**
 * Model for login credentials
 */
data class UsernamePasswordCredentials(val username: String, val password: String)

fun Route.auth() {
    val tokenToUsers = mutableMapOf<String, String?>()
    route("/auth") {
        post("/signin") {
            val user = call.receive<UsernamePasswordCredentials>()
            val receivedUser = UserService.getByUsername(user.username)
            receivedUser?.let {
                if (it.username == user.username &&
                    BCryptPasswordEncoder(12).matches(user.password, it.password)
                ) {

                    val tokenedUser = it.applyToken("token")
                    tokenToUsers[it.uuid.toString()] = tokenedUser.temporaryToken
                    Timer().schedule(100000L) { tokenToUsers.remove(it.uuid.toString()) }
                    call.respond(tokenedUser.hidePassword())
                } else {
                    call.respond(HttpStatusCode.Forbidden)
                }
            } ?: call.respond(HttpStatusCode.BadRequest)
        }

        get("/{token}") {
            // TODO: Change this param to header
            call.parameters["token"]?.let { token ->
                val user = tokenToUsers.filter { it.value == token }.keys.singleOrNull() ?: error("user not found")
                call.respond(
                    UserService.get(user.toUUID())?.applyToken(token) ?: HttpStatusCode.BadRequest
                )
            } ?: call.respond(HttpStatusCode.BadRequest)
        }

        post("/signup") {
            val user = call.receive<User>()
            val hashedUser = user.hashPassword()
            println("signup -> $hashedUser")
            if (UserService.add(hashedUser)) {
                call.respond(HttpStatusCode.Created)
            } else {
                call.respond(HttpStatusCode.InternalServerError)
            }
        }
    }
}
