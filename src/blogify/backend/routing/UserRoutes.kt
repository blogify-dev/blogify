package blogify.backend.routing

import blogify.common.util.assertGet
import blogify.reflect.sanitize
import blogify.reflect.slice
import blogify.backend.auth.handling.authenticated
import blogify.backend.auth.handling.optionallyAuthenticated
import blogify.backend.database.tables.Events
import blogify.backend.database.tables.Users
import blogify.reflect.entity.database.handling.query
import blogify.backend.pipelines.wrapping.ApplicationContext
import blogify.backend.resources.user.User
import blogify.backend.search.Typesense
import blogify.backend.search.ext.asSearchView
import blogify.backend.pipelines.*
import blogify.backend.routing.handling.*
import blogify.backend.util.getOr404OrPipelineError

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
                    authPredicate = { user, manipulated -> user == manipulated }
                )
            }
        }

        patch("/{uuid}") {
            requestContext(applicationContext) {
                updateResource<User> (
                    authPredicate = { user, replaced -> user == replaced }
                )
            }
        }

        get("/byUsername/{username}") {
            requestContext(applicationContext) {
                val username = param("username")
                val selectedPropertyNames = optionalParam("fields")?.split(",")?.toSet()

                val matchingUser = repository<User>().getOneMatching(this) { Users.username eq username }
                    .getOr404OrPipelineError()

                optionallyAuthenticated { user ->
                    applyDefaultComputedPropertyResolver(matchingUser, user)

                    selectedPropertyNames?.let { props ->
                        call.respond(matchingUser.slice(props))
                    } ?: call.respond(matchingUser.sanitize())
                }
            }
        }

        post("/upload/{uuid}") {
            requestContext(applicationContext) {
                uploadToResource<User>(authPredicate = { user, manipulated -> user == manipulated })
            }
        }

        delete("/upload/{uuid}") {
            requestContext(applicationContext) {
                deleteUpload<User>(authPredicate = { user, manipulated -> user == manipulated })
            }
        }

        post("/{uuid}/toggleAdmin") {
            requestContext(applicationContext, toggleUserAdmin)
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

                authenticated { user ->

                    val hasAlreadyFollowed = query {
                        follows.select {
                            (follows.follower eq user.uuid) and (follows.following eq following.uuid)
                        }.count().toInt()
                    }.get() == 1

                    if (!hasAlreadyFollowed) {
                        query {
                            follows.insert {
                                it[follower] = user.uuid
                                it[Users.Follows.following] = following.uuid
                            }
                        }
                    } else {
                        query {
                            follows.deleteWhere {
                                (follows.follower eq user.uuid) and (follows.following eq following.uuid)
                            }
                        }
                    }
                    following.FollowedEvent(user).send(this)

                    call.respond(HttpStatusCode.OK)
                }
            }
        }

        route("/me") {

            get {
                requestContext(applicationContext) {
                    authenticated { user ->
                        applyDefaultComputedPropertyResolver(user, user)

                        call.respond(optionalParam("fields")?.split(",")?.toSet()?.let { user.slice(it) }
                                ?: user.sanitize(excludeUndisplayed = true))
                    }
                }
            }

            route("drafts") {
                get("articles") {
                    requestContext(applicationContext, getUserDraftArticles)
                }
            }

            get("/notifications") {
                requestContext(applicationContext) {
                    authenticated { user ->
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
