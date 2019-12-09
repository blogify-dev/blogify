package blogify.backend.routes.users

import blogify.backend.database.Users
import blogify.backend.resources.User
import blogify.backend.resources.models.eqr
import blogify.backend.resources.reflect.sanitize
import blogify.backend.resources.reflect.slice
import blogify.backend.routes.handling.*
import blogify.backend.routes.pipelines.pipeline
import blogify.backend.search.Typesense
import blogify.backend.search.ext.asSearchView
import blogify.backend.services.UserService
import blogify.backend.services.models.Service
import blogify.backend.util.toUUID

import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.*

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
            deleteWithId (
                fetch = UserService::get,
                delete = UserService::delete,
                authPredicate = { user, manipulated -> user eqr manipulated },
                doAfter = { id ->
                    Typesense.deleteResource<User>(id.toUUID())
                }
            )
        }

        patch("/{uuid}") {
            updateWithId (
                fetch = UserService::get,
                authPredicate = { _, _ -> true },
                doAfter = { replacement ->
                    Typesense.updateResource(replacement)
                }
            )
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

        post("/upload/{uuid}") {
            uploadToResource (
                fetch         = UserService::get,
                authPredicate = { user, manipulated -> user eqr manipulated }
            )
        }

        delete("/upload/{uuid}") {
            deleteOnResource (
                fetch         = UserService::get,
                authPredicate = { user, manipulated -> user eqr manipulated }
            )
        }

        get("/search") {
            pipeline("q") { (query) ->
                call.respond(Typesense.search<User>(query).asSearchView())
            }
        }

    }

}
