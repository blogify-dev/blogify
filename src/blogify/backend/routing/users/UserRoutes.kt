package blogify.backend.routing.users

import blogify.backend.auth.handling.runAuthenticated
import blogify.backend.database.Users
import blogify.backend.database.handling.query
import blogify.backend.pipelines.wrapping.ApplicationContext
import blogify.backend.resources.User
import blogify.backend.resources.models.eqr
import blogify.backend.resources.reflect.sanitize
import blogify.backend.resources.reflect.slice
import blogify.backend.routing.handling.deleteResource
import blogify.backend.routing.handling.deleteUpload
import blogify.backend.routing.handling.fetchAllResources
import blogify.backend.routing.handling.fetchResource
import blogify.backend.pipelines.obtainResource
import blogify.backend.pipelines.optionalParam
import blogify.backend.routing.handling.respondExceptionMessage
import blogify.backend.routing.handling.updateResource
import blogify.backend.routing.handling.uploadToResource
import blogify.backend.pipelines.param
import blogify.backend.pipelines.requestContext
import blogify.backend.search.Typesense
import blogify.backend.search.ext.asSearchView
import blogify.backend.persistence.models.Repository
import blogify.backend.util.toUUID

import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.*

import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select

/**
 * Defines the API routes for interacting with [users][User].
 */
fun Route.makeUserRoutes(applicationContext: ApplicationContext) {

    route("/users") {

        get("/") {
            requestContext(applicationContext) {
                fetchAllResources<User>()
            }
        }

        get("/{uuid}") {
            requestContext(applicationContext) {
                fetchResource<User>()
            }
        }

        delete("/{uuid}") {
            requestContext(applicationContext) {
                deleteResource<User> (
                    authPredicate = { user, manipulated -> user eqr manipulated }
                )
            }
        }

        patch("/{uuid}") {
            requestContext(applicationContext) {
                updateResource<User> (
                    authPredicate = { user, replaced -> user eqr replaced }
                )
            }
        }

        get("/byUsername/{username}") {
            requestContext(applicationContext) {
                val username = param("username")
                val selectedPropertyNames = optionalParam("fields")?.split(",")?.toSet()

                repository<User>().getMatching { Users.username eq username }.fold(
                    success = {
                        val user = it.single()
                        try {
                            selectedPropertyNames?.let { props ->

                                call.respond(user.slice(props))

                            } ?: call.respond(user.sanitize())
                        } catch (bruhMoment: Repository.Exception) {
                            call.respondExceptionMessage(bruhMoment)
                        }
                    },
                    failure = { call.respondExceptionMessage(it) }
                )
            }
        }

        post("/upload/{uuid}") {
            requestContext(applicationContext) {
                uploadToResource<User> (
                    authPredicate = { user, manipulated -> user eqr manipulated }
                )
            }
        }

        delete("/upload/{uuid}") {
            requestContext(applicationContext) {
                deleteUpload<User>(authPredicate = { user, manipulated -> user eqr manipulated })
            }
        }

        get("/search") {
            requestContext(applicationContext) {
                val query = param("q")

                call.respond(Typesense.search<User>(query).asSearchView(this))
            }
        }

        post("{uuid}/follow") {
            val follows = Users.Follows

            requestContext(applicationContext) {
                val id = param("uuid")

                val following = obtainResource<User>(id.toUUID())

                runAuthenticated { subject ->

                    val hasAlreadyFollowed = query {
                        follows.select {
                            (follows.follower eq subject.uuid) and (follows.following eq following.uuid)
                        }.count().toInt()
                    }.get() == 1

                    if (!hasAlreadyFollowed) {
                        query {
                            follows.insert {
                                it[Users.Follows.follower] = subject.uuid
                                it[Users.Follows.following] = following.uuid
                            }
                        }
                    } else {
                        query {
                            follows.deleteWhere {
                                (follows.follower eq subject.uuid) and (follows.following eq following.uuid)
                            }
                        }
                    }
                    following.FollowedEvent(subject).send(this)

                    call.respond(HttpStatusCode.OK)
                }
            }


        }

    }

}
