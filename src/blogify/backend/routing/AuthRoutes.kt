package blogify.backend.routing

import blogify.backend.annotations.check
import blogify.backend.auth.encoder
import blogify.backend.auth.jwt.generateJWT
import blogify.backend.auth.jwt.validateJwt
import blogify.backend.database.Users
import blogify.backend.pipelines.param
import blogify.backend.pipelines.requestContext
import blogify.backend.pipelines.wrapping.ApplicationContext
import blogify.backend.pipelines.wrapping.RequestContext
import blogify.backend.resources.User
import blogify.backend.resources.static.models.StaticResourceHandle
import blogify.backend.routing.handling.respondExceptionMessage
import blogify.backend.search.Typesense
import blogify.backend.persistence.models.Repository
import blogify.backend.util.*
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
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
    val password: @check(".{8,}") String,
    val name:     String,
    val email:    String
) {
    /**
     * Creates a [user][User] from the [credentials][RegisterCredentials].
     * @return The created user
     */
    suspend fun createUser(requestContext: RequestContext): User {
        val created = User (
            username = this.username,
            password = this.password.hash(),
            email = this.email,
            name = this.name,
            profilePicture = StaticResourceHandle.None(ContentType.Image.PNG),
            coverPicture = StaticResourceHandle.None(ContentType.Image.PNG)
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

fun Route.auth(applicationContext: ApplicationContext) {

    route("/auth") {

        post("/signin") {
            requestContext(applicationContext) {
                val credentials = call.receive<LoginCredentials>()
                val matchingCredentials = repository<User>().getMatching(this) { Users.username eq credentials.username }

                matchingCredentials.fold (
                    success = { set ->
                        set.foldForOne ( // We got a set of matching users
                            one = { singleUser ->
                                if (credentials.matchFor(singleUser)) {
                                    val token = generateJWT(singleUser)

                                    call.respond(object { @Suppress("unused") val token = token })
                                } else {
                                    call.respond(HttpStatusCode.Forbidden, reason("username/password invalid")) // Password doesn't match
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
        }

        get("/{token}") {
            requestContext(applicationContext) {
                val token = param("token")
                validateJwt(this, token).fold (
                    success = { call.respond( object { @Suppress("unused") val uuid = it.uuid }) },
                    failure = { call.respondExceptionMessage(Repository.Exception(BException(it))) }
                )
            }
        }

        post("/signup") {
            requestContext(applicationContext) {
                val credentials = call.receive<RegisterCredentials>()
                val createdUser = credentials.createUser(this)
                call.respond(createdUser)
            }
        }

    }
}
