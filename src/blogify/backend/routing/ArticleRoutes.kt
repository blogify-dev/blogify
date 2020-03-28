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
import blogify.backend.routing.handling.createResource
import blogify.backend.routing.handling.deleteResource
import blogify.backend.routing.handling.fetchAllResources
import blogify.backend.routing.handling.fetchResource
import blogify.backend.routing.handling.getValidations
import blogify.backend.routing.handling.respondExceptionMessage
import blogify.backend.routing.handling.updateResource
import blogify.backend.pipelines.obtainResource
import blogify.backend.pipelines.param
import blogify.backend.pipelines.requestContext
import blogify.backend.resources.User
import blogify.backend.search.Typesense
import blogify.backend.search.ext.asSearchView
import blogify.backend.persistence.models.Repository
import blogify.backend.routing.handling.fetchResourceListing
import blogify.backend.util.getOrPipelineError
import blogify.backend.util.reason
import blogify.backend.util.toUUID

import io.ktor.http.HttpStatusCode
import io.ktor.routing.*
import io.ktor.response.respond

import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select

fun Route.makeArticleRoutes(applicationContext: ApplicationContext) {

    route("/articles") {

        get("/") {
            requestContext(applicationContext) {
                fetchAllResources<Article>()
            }

        }

        get("/listing") {
            requestContext(applicationContext) {
                fetchResourceListing<Article>()
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

        get("/forUser/{username}") {
            requestContext(applicationContext) {
            val params = call.parameters
            val username = params["username"] ?: error("Username is null")
            val selectedPropertyNames = params["fields"]?.split(",")?.toSet()

            repository<User>().getMatching { Users.username eq username }.fold(
                success = {
                    repository<Article>().getMatching { Articles.createdBy eq it.single().uuid }.fold(
                        success = { articles ->
                            try {
                                selectedPropertyNames?.let { props ->

                                    call.respond(articles.map { it.slice(props) })

                                } ?: call.respond(articles.map { it.sanitize() })
                            } catch (bruhMoment: Repository.Exception) {
                                call.respondExceptionMessage(bruhMoment)
                            }
                        },
                        failure = { call.respondExceptionMessage(it) }
                    )
                },
                failure = { call.respondExceptionMessage(it) }
            )
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
                    val userHandle = Article::class.cachedPropMap().ok()["createdBy"] ?: error("a")
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

        articleComments(applicationContext)

    }

}
