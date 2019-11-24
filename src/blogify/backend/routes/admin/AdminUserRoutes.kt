@file:Suppress("DuplicatedCode")

package blogify.backend.routes.admin

import blogify.backend.resources.reflect.sanitize
import blogify.backend.routes.handling.deleteWithId
import blogify.backend.routes.handling.respondExceptionMessage
import blogify.backend.routes.handling.updateWithId
import blogify.backend.services.UserService
import blogify.backend.util.TYPESENSE_API_KEY
import blogify.backend.util.toUUID
import io.ktor.application.call
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.*

fun Route.adminUsers() {
    route("/users") {

        delete("/{uuid}") {
            deleteWithId(
                fetch = UserService::get,
                delete = UserService::delete,
                authPredicate = { user, _ -> user.isAdmin },
                doAfter = { id ->
                    HttpClient().use { client ->
                        client.delete<String> {
                            url("http://ts:8108/collections/users/documents/$id")
                            header("X-TYPESENSE-API-KEY", TYPESENSE_API_KEY)
                        }.also { println(it) }
                    }
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

