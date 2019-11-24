@file:Suppress("DuplicatedCode")

package blogify.backend.routes.admin

import blogify.backend.routes.handling.deleteWithId
import blogify.backend.routes.handling.updateWithId
import blogify.backend.services.UserService
import blogify.backend.util.TYPESENSE_API_KEY
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.header
import io.ktor.client.request.url
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

        post("/{uuid}") {
            /*TODO: Make admin*/
        }
    }
}

