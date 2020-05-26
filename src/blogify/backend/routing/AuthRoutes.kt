package blogify.backend.routing

import blogify.reflect.annotations.check
import blogify.backend.auth.encoder
import blogify.backend.auth.jwt.generateJWT
import blogify.backend.database.tables.Users
import blogify.backend.pipelines.requestContext
import blogify.backend.pipelines.wrapping.ApplicationContext
import blogify.backend.pipelines.wrapping.RequestContext
import blogify.backend.resources.user.User
import blogify.backend.resources.static.models.StaticFile
import blogify.backend.search.Typesense
import blogify.backend.util.*

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route

/**
 * Model for login credentials
 */
data class LoginCredentials (
    val username: String,
    val password: String
) {

    /**
     * Checks the [credentials][LoginCredentials] against a [user][User]
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
    val password: @check(".{8,}") String,
    val name:     String,
    val email:    String
) {
    /**
     * Creates a [user][User] from the [credentials][RegisterCredentials].
     * @return The created user
     */
    suspend fun createUser(requestContext: RequestContext): User {
        val created = User(
            username = this.username,
            password = this.password.hash(),
            email = this.email,
            name = this.name,
            profilePicture = StaticFile.None(ContentType.Image.PNG),
            coverPicture = StaticFile.None(ContentType.Image.PNG)
        )

        requestContext.repository<User>().add(created).fold (
            success = { user -> Typesense.uploadResource(user) },
            failure = {
                error("$created: signup couldn't create user\nError:$it")
            }
        )

        return created
    }

}

fun Route.makeAuthRoutes(applicationContext: ApplicationContext) {

    route("/auth") {

        post("/signin") {
            requestContext(applicationContext) {
                val credentials = call.receive<LoginCredentials>()
                val dbCredentials = repository<User>().getOneMatching(this) { Users.username eq credentials.username }
                    .getOr404OrPipelineError(this)

                if (credentials.matchFor(dbCredentials)) {
                    val token = generateJWT(dbCredentials)

                    call.respond(object { val token = token })
                } else {
                    call.respond(HttpStatusCode.Forbidden, reason("username/password invalid"))
                }
            }
        }

        post("/signup") {
            requestContext(applicationContext) {
                val credentials = call.receive<RegisterCredentials>()
                val createdUser = credentials.createUser(this)

                call.respond(@Suppress("unused") object {
                    val user  = createdUser
                    val token = generateJWT(createdUser)
                })
            }
        }

    }
}
