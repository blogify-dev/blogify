package blgoify.backend.routes

import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.delete
import io.ktor.routing.route

import blgoify.backend.resources.User
import blgoify.backend.services.UserService
import blgoify.backend.routes.handling.handleResourceFetch
import blgoify.backend.routes.handling.handleResourceCreation
import blgoify.backend.routes.handling.handleResourceDeletion
import blgoify.backend.routes.handling.handleResourceFetchAll

/**
 * Defines the API routes for interacting with [users][User].
 */
fun Route.users() {

    route("/users") {

        get("/") {
            handleResourceFetchAll(UserService::getAll)
        }

        get("/{uuid}") {
            handleResourceFetch(UserService::get)
        }

        delete("/{uuid}") {
            handleResourceDeletion(UserService::delete)
        }

        post("/") {
            handleResourceCreation(UserService::add)
        }

    }

}