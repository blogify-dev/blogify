@file:Suppress("DuplicatedCode")

package blogify.backend.routes.admin

import blogify.backend.resources.models.eqr
import blogify.backend.resources.reflect.sanitize
import blogify.backend.routes.handling.deleteWithId
import blogify.backend.routes.handling.respondExceptionMessage
import blogify.backend.routes.handling.updateWithId
import blogify.backend.services.UserService
import blogify.backend.util.toUUID
import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.*

fun Route.adminUsers() {
    route("/users") {

        delete("/{uuid}") {
            deleteWithId(
                fetch = UserService::get,
                delete = UserService::delete,
                authPredicate = { user, toBeDeleted -> user.isAdmin && !(user eqr toBeDeleted) },
                doAfter = {
                    /*TODO: Update typesense index*/
                }
            )
        }

        patch("/{uuid}") {
            updateWithId(UserService::update, UserService::get, authPredicate = { _, _ -> true })
        }

        patch("/makeAdmin/{uuid}") {
            val uuid = call.parameters["uuid"] ?: error("UUID is null")
            UserService.get(call, uuid.toUUID()).fold(
                success = { user ->
                    val modified = user.copy(isAdmin = true)
                    UserService.update(modified).fold(
                        success = { call.respond(it.sanitize()) },
                        failure = call::respondExceptionMessage
                    )
                },
                failure = call::respondExceptionMessage
            )
        }
    }
}

