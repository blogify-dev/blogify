package blogify.backend.routes.users

import blogify.backend.database.Users
import blogify.backend.database.handling.query
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
import org.jetbrains.exposed.sql.select

/**
 * Defines the API routes for interacting with [users][User].
 */
fun Route.users() {

    route("/users") {

        get("/") {
            fetchAllResources<User>()
        }

        get("/{uuid}") {
            fetchResource<User>()
        }

        delete("/{uuid}") {
            deleteUpload<User> (
                authPredicate = { user, manipulated -> user eqr manipulated }
            )
        }

        patch("/{uuid}") {
            updateResource<User> (
                authPredicate = { user, replaced -> user eqr replaced }
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
            uploadToResource<User> (
                authPredicate = { user, manipulated -> user eqr manipulated }
            )
        }

        delete("/upload/{uuid}") {
            deleteUpload<User>(authPredicate = { user, manipulated -> user eqr manipulated })
        }

        get("/search") {
            pipeline("q") { (query) ->
                call.respond(Typesense.search<User>(query).asSearchView())
            }
        }

        get("/{uuid}/follows") {
            pipeline("uuid") { (uuid) ->
                val followings = query { Users.Follows.select {
                    Users.Follows.follower eq uuid.toUUID()
                }.toList().map { Users.Follows.convert(call, it).get() } }.get()

                call.respond(followings)
            }
        }

        post("/{uuid}/follow") {

        }

    }

}
