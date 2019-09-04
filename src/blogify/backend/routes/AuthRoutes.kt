package blogify.backend.routes

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route

import blogify.backend.auth.encoder
import blogify.backend.database.Users
import blogify.backend.resources.User
import blogify.backend.routes.handling.respondExceptionMessage
import blogify.backend.services.UserService
import blogify.backend.util.foldForOne
import blogify.backend.util.hash
import blogify.backend.util.letIn
import blogify.backend.util.singleOrNullOrError

import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

import java.util.Base64

import kotlin.random.Random

/**
 * Model for login credentials
 */
data class LoginCredentials (
    val username: String,
    val password: String
) {

    /**
     * Checks the [credentials][LoginCredentials] against a [user][User].
     *
     * @param user the user to check the credentials against
     *
     * @return `true` if the [credentials][LoginCredentials] match
     */
    fun matchFor(user: User): Boolean {
        return (username == user.username && encoder.matches(password, user.password))
    }
}

data class RegisterCredentials (
    val username: String,
    val password: String,
    val name:     String,
    val email:    String
) {
    /**
     * Creates a [user][User] from the [credentials][RegisterCredentials].
     * @return The created user
     */
    suspend fun createUser(): User = User(
        username = this.username,
        password = this.password.hash()
    ).also { created -> // We need to add user info and register the new object

        UserService.add(created).fold(
            success = {},
            failure = {
                error("$it: signup couldn't create user")
            }
        )

        transaction {
            Users.UserInfo.insert {
                it[user]  = created.uuid
                it[name]  = this@RegisterCredentials.name
                it[email] = this@RegisterCredentials.email
            }
        }

        return created
    }
}

val validTokens = mutableMapOf<User, String>()

fun Route.auth() {

    route("/auth") {

        post("/signin") {

            val credentials = call.receive<LoginCredentials>()
            val matchingCredentials = UserService.getMatching(Users) { Users.username eq credentials.username }

            matchingCredentials.fold (
                success = { set ->
                    set.foldForOne (
                        one = { singleUser ->
                            if (credentials.matchFor(singleUser)) {
                                val token = Base64
                                    .getUrlEncoder() // Generate token
                                    .withoutPadding()
                                    .encodeToString(Random.Default.nextBytes(64))

                                validTokens[singleUser] = token
                                validTokens.letIn(3600 * 1000L) { it.remove(singleUser) }

                                call.respond(token)
                            } else {
                                call.respond(HttpStatusCode.Forbidden) // Password doesn't match
                            }
                        }, multiple = {
                            call.respond(HttpStatusCode.InternalServerError)
                        }, none = {
                            call.respond(HttpStatusCode.NotFound)
                        })
                },
                failure = { ex ->
                    call.respondExceptionMessage(ex)
                }
            )

        }

        get("/{token}") {
            call.parameters["token"]?.let { token ->

                val user = validTokens
                    .filter { it.value == token } // Check for user with token
                    .keys.singleOrNullOrError() ?: error("user not found")

                // User is found and only exists once

                call.respond(user)
            } ?: call.respond(HttpStatusCode.BadRequest)
        }

        post("/signup") {
            val credentials = call.receive<RegisterCredentials>()
            println("credentials -> $credentials")
            val createdUser = credentials.createUser()
            call.respond(createdUser)
        }

    }
}
