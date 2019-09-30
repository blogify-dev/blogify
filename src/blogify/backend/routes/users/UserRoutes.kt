package blogify.backend.routes.users

import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.delete
import io.ktor.routing.route

import blogify.backend.resources.User
import blogify.backend.services.UserService
import blogify.backend.routes.handling.fetchWithIdAndRespond
import blogify.backend.routes.handling.createWithResource
import blogify.backend.routes.handling.deleteWithId
import blogify.backend.routes.handling.fetchAndRespondWithAll

/**
 * Defines the API routes for interacting with [users][User].
 */
fun Route.users() {

    route("/users") {

        get("/") {
            fetchAndRespondWithAll(UserService::getAll)
        }

        get("/{uuid}") {
            fetchWithIdAndRespond(UserService::get)
        }

        delete("/{uuid}") {
            deleteWithId(UserService::get, UserService::delete)
        }

        post("/") {
            createWithResource(UserService::add)
        }

    }

}