@file:Suppress("DuplicatedCode")

package blogify.backend.routing

import blogify.backend.auth.handling.runAuthenticated
import blogify.backend.database.Articles
import blogify.backend.database.Users
import blogify.backend.database.handling.query
import blogify.backend.pipelines.wrapping.ApplicationContext
import blogify.backend.resources.Article
import blogify.backend.resources.models.eqr
import blogify.backend.resources.reflect.cachedPropMap
import blogify.backend.resources.reflect.models.ext.ok
import blogify.backend.resources.reflect.sanitize
import blogify.backend.resources.reflect.slice
import blogify.backend.pipelines.obtainResource
import blogify.backend.pipelines.param
import blogify.backend.pipelines.requestContext
import blogify.backend.resources.User
import blogify.backend.search.Typesense
import blogify.backend.search.ext.asSearchView
import blogify.backend.persistence.models.Repository
import blogify.backend.pipelines.optionalParam
import blogify.backend.routing.handling.*
import blogify.backend.util.getOrPipelineError
import blogify.backend.util.reason
import blogify.backend.util.toUUID

import io.ktor.http.HttpStatusCode
import io.ktor.routing.*
import io.ktor.response.respond
import org.jetbrains.exposed.sql.*

fun Route.makeArticleRoutes() {

    route("/articles") {

        get("/") {
            requestContext {
                fetchResourceListing<Article>(orderBy = Articles.isPinned, sortOrder = SortOrder.DESC)
            }
        }

        get("/{uuid}") {
            requestContext {
                fetchResource<Article>()
            }
        }

        val likes = Articles.Likes

        get("/{uuid}/like") {
            requestContext {
                val id = param("uuid")

                runAuthenticated { subject ->
                    val article = obtainResource<Article>(id.toUUID())

                    val liked = query {
                        likes.select {
                            (likes.article eq article.uuid) and (likes.user eq subject.uuid) }.count().toInt()
                    }.getOrPipelineError() == 1

                    call.respond(liked)
                }
            }
        }

        post("/{uuid}/like") {
            requestContext {
                val id = param("uuid")

                runAuthenticated { subject ->
                    val articleToLike = obtainResource<Article>(id.toUUID())

                    // Figure whether the article was already liked by the user
                    val alreadyLiked = query {
                        likes.select {
                            (likes.article eq articleToLike.uuid) and (likes.user eq subject.uuid) }.count().toInt()
                    }.getOrPipelineError() == 1

                    if (!alreadyLiked) { // Add a like if none were present
                        query {
                            likes.insert {
                                it[article] = articleToLike.uuid
                                it[user]    = subject.uuid
                            }
                        }.getOrPipelineError(HttpStatusCode.InternalServerError, "couldn't like article")

                        call.respond(HttpStatusCode.OK, reason("article liked"))
                    } else { // Remove an existing like if there was one
                        query {
                            likes.deleteWhere {
                                (likes.article eq articleToLike.uuid) and (likes.user eq subject.uuid)
                            }
                        }.getOrPipelineError(HttpStatusCode.InternalServerError, "couldn't unlike article")

                        call.respond(HttpStatusCode.OK, reason("article unliked"))
                    }
                }
            }
        }

        post("/{uuid}/pin") {
            requestContext {
                val id = param("uuid")
                val article = repository<Article>().get(this, id.toUUID()).get()

                runAuthenticated(predicate = { it.isAdmin }) {
                    Articles.update(article.copy(isPinned = !article.isPinned)).also {
                        call.respond(HttpStatusCode.OK)
                    }
                }
            }
        }

        get("/forUser/{username}/") {
            requestContext {
                val username = param("username")

                repository<User>().getMatching { Users.username eq username }.fold(
                    success = {
                        fetchResourceListing<Article>(Articles.uuid, SortOrder.ASC) { Articles.createdBy eq it.single().uuid }

                    },
                    failure = { call.respondExceptionMessage(it) }
                )
            }
        }


        delete("/{uuid}") {
            requestContext {
                deleteResource<Article> (
                    authPredicate = { user, article -> article.createdBy == user }
                )
            }
        }

        patch("/{uuid}") {
            requestContext {
                updateResource<Article> (
                    authPredicate = { user, article -> article.createdBy eqr user }
                )
            }
        }

        post("/") {
            requestContext {
                createResource<Article> (
                    authPredicate = { user, article -> article.createdBy eqr user }
                )
            }
        }

        get("/search") {
            requestContext {
                search<Article>(optionalParam("byUser")?.toUUID()?.let { mapOf((Article::class.cachedPropMap().ok()["createdBy"] ?: error("a")) to it) } ?: emptyMap())
            }
        }

        get("_validations") {
            requestContext {
                getValidations<Article>()
            }
        }

        articleComments()

    }

}
