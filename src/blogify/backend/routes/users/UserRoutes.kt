package blogify.backend.routes.users

import blogify.backend.database.Users
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.delete
import io.ktor.routing.route

import blogify.backend.resources.User
import blogify.backend.resources.slicing.sanitize
import blogify.backend.resources.slicing.slice
import blogify.backend.routes.handling.*
import blogify.backend.services.UserService
import blogify.backend.services.models.Service
import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.patch

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

        patch("/{uuid}") {
            updateWithId(UserService::update, UserService::get, authPredicate = { _, _ -> true })
        }

        get("/byUsername/{username}") {
            val params = call.parameters
            val username = params["username"] ?: error("Username is null")
            val selectedPropertyNames = params["fields"]?.split(",")?.toSet()

            UserService.getMatching { Users.username eq username }.fold(
                success = {
                    val user = it.single()
                    try {
                        selectedPropertyNames?.let { props ->

                            call.respond(user.slice(props))

                        } ?: call.respond(user.sanitize())
                    } catch (bruhMoment: Service.Exception) {
                        call.respondExceptionMessage(bruhMoment)
                    }
                },
                failure = { call.respondExceptionMessage(it) }
            )

        }

    }

}
