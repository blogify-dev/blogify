package blogify.backend.routes.users

import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.delete
import io.ktor.routing.route

import blogify.backend.resources.User
import blogify.backend.routes.handling.*
import blogify.backend.services.UserService

/**
 * Defines the API routes for interacting with [users][User].
 */
fun Route.users() {

    route("/users") {

        get("/") {
            fetchAll(UserService::getAll)
        }

        get("/{uuid}") {
            fetchWithId(UserService::get)
        }

        delete("/{uuid}") {
            deleteWithId(UserService::get, UserService::delete)
        }

        post("/") {
            createWithResource(UserService::add)
        }

    }

}
