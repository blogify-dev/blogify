package blgoify.backend.routes

import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route

import blgoify.backend.resources.User
import blgoify.backend.services.UserService
import blgoify.backend.routes.handling.handleResourceFetch
import blgoify.backend.routes.handling.handleResourceCreation
import blgoify.backend.routes.handling.handleResourceFetchAll

/**
 * Defines the API routes for interacting with [users][User].
 */
fun Route.users() {

    route("/users") {

        // GET all users

        get("/") {
            handleResourceFetchAll(UserService::getAll)
        }

        // GET a specific user

        get("/{uuid}") {
            handleResourceFetch(UserService::get)
        }

        // POST a new user

        post("/") {
            handleResourceCreation(UserService::add)
        }

    }

}