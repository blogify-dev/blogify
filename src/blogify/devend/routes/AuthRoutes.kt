package blogify.devend.routes

import blogify.backend.auth.jwt.generateJWT
import blogify.backend.database.tables.Users
import blogify.backend.pipelines.optionalParam
import blogify.backend.pipelines.requestContext
import blogify.backend.resources.user.User
import blogify.backend.util.getOrPipelineError
import blogify.common.util.toUUID
import blogify.devend.utils.user
import blogify.database.handling.query
import com.github.kittinunf.result.coroutines.getOrNull
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.delete
import io.ktor.routing.post
import io.ktor.routing.route
import org.jetbrains.exposed.sql.deleteAll

@ExperimentalStdlibApi
fun Route.authSeedRoutes() {
    route("/auth") {
        post("/login") {
            requestContext {
                val asAdmin = optionalParam("asAdmin")?.toIntOrNull()?.takeIf { it in 0..1 }?.let { it == 1 } ?: false
                val obtained = repository<User>().getOneMatching(this) { Users.uuid eq "3aee057d-e6e1-4b13-a0a7-0ea8f1f841f5".toUUID() }
                    .getOrNull() ?: repository<User>().add(user.copy(isAdmin = asAdmin)).getOrPipelineError()

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

        delete {
            requestContext {
                val amount = query { Users.deleteAll() }.getOrPipelineError()
                call.respond(HttpStatusCode.OK, object { val amount = amount })
            }
        }
    }
}
