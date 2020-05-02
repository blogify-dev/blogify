package blogify.backend.routing

import blogify.backend.auth.handling.autenticated
import blogify.backend.database.tables.Events
import blogify.backend.database.tables.Users
import blogify.backend.database.handling.query
import blogify.backend.pipelines.wrapping.ApplicationContext
import blogify.backend.resources.user.User
import blogify.backend.resources.models.eqr
import blogify.reflect.sanitize
import blogify.reflect.slice
import blogify.backend.search.Typesense
import blogify.backend.search.ext.asSearchView
import blogify.backend.persistence.models.Repository
import blogify.backend.pipelines.*
import blogify.backend.routing.handling.*
import blogify.backend.util.*

import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.*

import org.jetbrains.exposed.sql.*

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
                uploadToResource<User>(authPredicate = { user, manipulated -> user eqr manipulated })
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

        @Suppress("RemoveRedundantQualifierName")
        post("{uuid}/follow") {
            val follows = Users.Follows

            requestContext(applicationContext) {
                val id by queryUuid

                val following = obtainResource<User>(id)

                autenticated { subject ->

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

        route("/me") {

            get {
                requestContext(applicationContext) {
                    autenticated { user ->
                        call.respond(optionalParam("fields")?.split(",")?.toSet()?.let { user.slice(it) }
                                ?: user.sanitize(excludeUndisplayed = true))
                    }
                }
            }

            get("/notifications") {
                requestContext(applicationContext) {
                    autenticated { user ->
                        val count = optionalParam("limit")?.toIntOrNull()?.coerceAtMost(25) ?: 25

                        val notifications = query {
                            Events.select { Events.emitter eq user.uuid }
                                    .orderBy(Events.timestamp, SortOrder.DESC)
                                    .limit(count)
                                    .map { Events.convert(this, it) }
                                    .toList()
                        }.assertGet()

                        call.respond(notifications.takeIf { it.isNotEmpty() } ?: "[]")
                    }
                }
            }

            route("/settings") {
                get   { requestContext(applicationContext, function = getSettings) }
                patch { requestContext(applicationContext, function = updateSettings) }
            }

        }

    }

}