package blogify.devend.routes

import blogify.backend.auth.jwt.generateJWT
import blogify.backend.database.tables.Users
import blogify.backend.pipelines.requestContext
import blogify.backend.resources.static.models.StaticFile
import blogify.backend.resources.user.User
import blogify.backend.util.getOrPipelineError
import blogify.backend.util.hash
import blogify.common.util.toUUID
import com.github.kittinunf.result.coroutines.getOrNull
import io.ktor.http.ContentType
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route

private val user = User(
    username = "test_user",
    password = "test_pass".hash(),
    name = "test_name",
    email = "test@fuck.com",
    profilePicture = StaticFile.None(ContentType.Any),
    coverPicture = StaticFile.None(ContentType.Any),
    uuid = "3aee057d-e6e1-4b13-a0a7-0ea8f1f841f5".toUUID()
)

fun Route.authSeedRoutes() {
    route("/auth") {
        post("/login") {
            requestContext {
                val obtained = repository<User>().getOneMatching(this) { Users.uuid eq "3aee057d-e6e1-4b13-a0a7-0ea8f1f841f5".toUUID() }
                    .getOrNull() ?: repository<User>().add(user).getOrPipelineError()

                call.respond(@Suppress("unused") object {
                    val user  = obtained
                    val token = generateJWT(obtained)
                })
            }
        }

        post("/signup") {
            requestContext {
                val createdUser = repository<User>().add(user).getOrPipelineError()
                call.respond(@Suppress("unused") object {
                    val user  = createdUser
                    val token = generateJWT(createdUser)
                })
            }
        }
    }
}
