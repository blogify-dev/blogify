package blogify.backend.routes.users

import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.delete
import io.ktor.routing.route

import blogify.backend.resources.User
import blogify.backend.services.UserService
import blogify.backend.routes.handling.handleResourceFetch
import blogify.backend.routes.handling.handleResourceCreation
import blogify.backend.routes.handling.handleResourceDeletion
import blogify.backend.routes.handling.handleResourceFetchAll

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

        userInfo()

    }

}