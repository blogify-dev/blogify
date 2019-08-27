package blgoify.backend.routes

import blgoify.backend.auth.encoder
import blgoify.backend.database.Users
import blgoify.backend.resources.User
import blgoify.backend.services.UserService
import blgoify.backend.util.hash
import blgoify.backend.util.letIn
import blgoify.backend.util.singleOrNullOrError

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route

import java.util.Base64

import kotlin.random.Random

/**
 * Model for login credentials
 */
data class UsernamePasswordCredentials(val username: String, val password: String) {

    /**
     * Checks the [credentials][UsernamePasswordCredentials] against a [user][User].
     *
     * @param user the user to check the credentials against
     *
     * @return `true` if the [credentials][UsernamePasswordCredentials] match
     */
    fun matchFor(user: User): Boolean {
        return (username == user.username && encoder.matches(password, user.password))
    }

    /**
     * Creates a [user][User] from the [credentials][UsernamePasswordCredentials].
     */
    suspend fun createUser(): User = User (
            name     = this.username,
            username = this.username,
            password = this.password.hash()
        ).also { created ->
                if (!UserService.add(created)) {
                    error("signup couldn't create user")
                }
                return created
            }


}

val validTokens = mutableMapOf<User, String>()

fun Route.auth() {

    route("/auth") {

        post("/signin") {

            val credentials     = call.receive<UsernamePasswordCredentials>()
            val credentialsUser = UserService.getMatching(Users) { Users.username eq credentials.username }.singleOrNullOrError()

            credentialsUser?.let { user ->

                if (credentials.matchFor(user)) {
                    val token = Base64
                        .getUrlEncoder() // Generate token
                        .withoutPadding()
                        .encodeToString(Random.Default.nextBytes(64))

                    validTokens[user] = token
                    validTokens.letIn(3600 * 1000L) { it.remove(user) }

                    call.respond(token)
                } else {
                    call.respond(HttpStatusCode.Forbidden) // Password doesn't match
                }

            } ?: call.respond(HttpStatusCode.NotFound)

        }

        get("/{token}") {
            // TODO: Change this param to header
            call.parameters["token"]?.let { token ->

                val user = validTokens
                    .filter { it.value == token } // Check for user with token
                    .keys.singleOrNullOrError() ?: error("user not found")

                // User is found and only exists once

                call.respond(user)
            } ?: call.respond(HttpStatusCode.BadRequest)
        }

        post("/signup") {
            val credentials = call.receive<UsernamePasswordCredentials>()
            val createdUser = credentials.createUser()
            call.respond(createdUser)
        }

    }
}
