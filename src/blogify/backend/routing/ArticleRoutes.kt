@file:Suppress("DuplicatedCode")

package blogify.backend.routing

import blogify.backend.auth.handling.runAuthenticated
import blogify.backend.database.Articles
import blogify.backend.database.Users
import blogify.backend.database.handling.query
import blogify.backend.pipelines.wrapping.ApplicationContext
import blogify.backend.resources.Article
import blogify.backend.resources.models.eqr
import blogify.backend.pipelines.obtainResource
import blogify.backend.pipelines.param
import blogify.backend.pipelines.requestContext
import blogify.backend.search.Typesense
import blogify.backend.search.ext.asSearchView
import blogify.backend.pipelines.queryUuid
import blogify.backend.resources.reflect.extensions.okHandle
import blogify.backend.routing.handling.*
import blogify.backend.util.getOrPipelineError
import blogify.backend.util.reason
import blogify.backend.util.toUUID

import io.ktor.http.HttpStatusCode
import io.ktor.routing.*
import io.ktor.response.respond

import org.jetbrains.exposed.sql.*

fun Route.makeArticleRoutes(applicationContext: ApplicationContext) {

    route("/articles") {

        get("/") {
            requestContext(applicationContext) {
                fetchResourceListing<Article>(orderBy = Articles.isPinned, sortOrder = SortOrder.DESC)
            }
        }

        get("/user/{uuid}") {
            requestContext(applicationContext) {
                val id by queryUuid

                fetchResourceListing<Article> (
                    orderBy = Articles.isPinned,
                    sortOrder = SortOrder.DESC,
                    selectCondition = {
                        Articles.createdBy eq id
                    }
                )
            }
        }

        get("/{uuid}") {
            requestContext(applicationContext) {
                fetchResource<Article>()
            }
        }

        val likes = Articles.Likes

        get("/{uuid}/like") {
            requestContext(applicationContext) {
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
            requestContext(applicationContext) {
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
            requestContext(applicationContext) {
                val id = param("uuid")
                val article = repository<Article>().get(this, id.toUUID()).get()

                runAuthenticated(predicate = { it.isAdmin }) {
                    Articles.update(article.copy(isPinned = !article.isPinned)).also {
                        call.respond(HttpStatusCode.OK)
                    }
                }
            }
        }

        delete("/{uuid}") {
            requestContext(applicationContext) {
                deleteResource<Article> (
                    authPredicate = { user, article -> article.createdBy == user }
                )
            }
        }

        patch("/{uuid}") {
            requestContext(applicationContext) {
                updateResource<Article> (
                    authPredicate = { user, article -> article.createdBy eqr user }
                )
            }
        }

        post("/") {
            requestContext(applicationContext) {
                createResource<Article> (
                    authPredicate = { user, article -> article.createdBy eqr user }
                )
            }
        }

        get("/search") {
            requestContext(applicationContext) {
                val query = call.parameters["q"]!!
                val user = call.parameters["byUser"]?.toUUID()

                if (user != null) {
                    val userHandle = Article::createdBy.okHandle ?: error("a")
                    call.respond(Typesense.search<Article>(query, mapOf(userHandle to user)).asSearchView(this))
                } else {
                    call.respond(Typesense.search<Article>(query).asSearchView(this))
                }
            }
        }

        get("_validations") {
            requestContext(applicationContext) {
                getValidations<Article>()
            }
        }

        makeArticleCommentRoutes(applicationContext)

    }

}
