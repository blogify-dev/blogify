package blgoify.backend.routes

import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route

import blgoify.backend.resources.User
import blgoify.backend.services.UserService
import blgoify.backend.routes.handling.handleResourceFetch
import blgoify.backend.routes.handling.handleResourceCreation

/**
 * Defines the API routes for interacting with [users][User].
 */
fun Route.users() {

    route("/users") {

        // GET all users

        get("/") {
            call.respond(UserService.getAll())
        }

        // GET a specific user

        get("/{uuid}") {
            handleResourceFetch({ UserService.get(it) })
        }

        // POST a new user

        post("/") {
            handleResourceCreation(UserService::add)
        }

    }

}