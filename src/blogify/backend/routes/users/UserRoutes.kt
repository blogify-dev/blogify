package blogify.backend.routes.users

import blogify.backend.database.Users
import blogify.backend.resources.User
import blogify.backend.resources.models.eqr
import blogify.backend.resources.search.Search
import blogify.backend.resources.slicing.sanitize
import blogify.backend.resources.slicing.slice
import blogify.backend.routes.handling.*
import blogify.backend.services.UserService
import blogify.backend.services.models.Service

import io.ktor.application.call
import io.ktor.client.HttpClient
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
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

        post("/profilePicture/{uuid}") {
            uploadToResource (
                fetch         = UserService::get,
                modify        = { r, h -> r.copy(profilePicture = h) },
                update        = UserService::update,
                authPredicate = { user, manipulated -> user eqr manipulated }
            )
        }

        delete("/profilePicture/{uuid}") {
            deleteOnResource (
                fetch         = UserService::get,
                authPredicate = { user, manipulated -> user eqr manipulated }
            )
        }

        get("/search") {
            val params = call.parameters
            val selectedPropertyNames = params["fields"]?.split(",")?.toSet()
            params["q"]?.let { query ->
                HttpClient { install(JsonFeature) }.use { client ->
                    val parsed = client.get<Search<Search.UserDocument>>("http://ts:8108/collections/users/documents/search?q=$query&query_by=username,name,email")
                    parsed.hits?.let { hits -> // Some hits
                        val hitResources = hits.map { it.document.user(call) }
                        try {
                            selectedPropertyNames?.let { props ->

                                call.respond(hitResources.map { it.slice(props) })

                            } ?: call.respond(hitResources.map { it.sanitize() })
                        } catch (bruhMoment: Service.Exception) {
                            call.respondExceptionMessage(bruhMoment)
                        }
                    } ?: call.respond(HttpStatusCode.NoContent) // No hits
                }
            }
        }

    }

}
