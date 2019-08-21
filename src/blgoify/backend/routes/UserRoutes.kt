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

import io.ktor.http.HttpStatusCode
import io.ktor.request.receive

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
            handleResourceFetch(UserService::get)
        }

        // POST a new user

        post("/") {
            /* handleSimpleResourceCreation<User> { UserService.add(it) }
               Doesn't work for now */
            val res: User

            try {
                res = call.receive<User>()
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError)
                e.printStackTrace()
                return@post
            }

            call.respond(if (UserService.add(res)) HttpStatusCode.Created else HttpStatusCode.InternalServerError)
        }

    }

}