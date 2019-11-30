@file:Suppress("DuplicatedCode")

package blogify.backend.routes.articles

import blogify.backend.auth.handling.runAuthenticated
import blogify.backend.database.Articles
import blogify.backend.database.Comments
import blogify.backend.database.Users
import blogify.backend.database.handling.query
import blogify.backend.resources.Article
import blogify.backend.resources.models.eqr
import blogify.backend.resources.reflect.cachedPropMap
import blogify.backend.resources.reflect.models.ext.ok
import blogify.backend.resources.reflect.sanitize
import blogify.backend.resources.reflect.slice
import blogify.backend.routes.handling.*
import blogify.backend.routes.pipelines.optional
import blogify.backend.routes.pipelines.pipeline
import blogify.backend.search.Typesense
import blogify.backend.search.ext.asSearchView
import blogify.backend.services.UserService
import blogify.backend.services.articles.ArticleService
import blogify.backend.services.articles.CommentService
import blogify.backend.services.models.Service
import blogify.backend.util.getOrPipelineError
import blogify.backend.util.reason
import blogify.backend.util.toUUID

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.routing.*
import io.ktor.response.respond

import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select

fun Route.articles() {

    route("/articles") {

        get("/") {
            fetchAll(ArticleService::getAll)
        }

        get("/{uuid}") {
            fetchWithId(ArticleService::get)
        }

        get("/{uuid}/likes") {
            pipeline("uuid") { (id) ->
                val numberOfLikes = query {
                    Articles.Likes.select { Articles.Likes.article eq id.toUUID() }.count()
                }.getOrPipelineError(HttpStatusCode.InternalServerError, "couldn't get like count")

                call.respond(numberOfLikes)
            }
        }

        post("/{uuid}/like") {
            pipeline("uuid") { (id) ->
                runAuthenticated {
                    val articleToLike = ArticleService.get(call, id.toUUID())
                        .getOrPipelineError(HttpStatusCode.NotFound, "couldn't fetch article")

                    // Figure whether or not the article was already liked by the user
                    val alreadyLiked = query {
                        Articles.Likes.select {
                            (Articles.Likes.article eq articleToLike.uuid) and (Articles.Likes.user eq subject.uuid) }.count()
                    }.getOrPipelineError() == 1;

                    if (!alreadyLiked) { // Add a like if none were present
                        query {
                            Articles.Likes.insert {
                                it[article] = articleToLike.uuid
                                it[user]    = subject.uuid
                            }
                        }.getOrPipelineError(HttpStatusCode.InternalServerError, "couldn't like article")

                        call.respond(HttpStatusCode.OK, reason("article liked"))
                    } else { // Remove an existing like if there was one
                        query {
                            Articles.Likes.deleteWhere {
                                (Articles.Likes.article eq articleToLike.uuid) and (Articles.Likes.user eq subject.uuid)
                            }
                        }.getOrPipelineError(HttpStatusCode.InternalServerError, "couldn't unlike article")

                        call.respond(HttpStatusCode.OK, reason("article unliked"))
                    }
                }
            }
        }

        get("/{uuid}/commentCount") {
            countReferringToResource(ArticleService::get, CommentService::getReferring, Comments.article)
        }

        get("/forUser/{username}") {

            val params = call.parameters
            val username = params["username"] ?: error("Username is null")
            val selectedPropertyNames = params["fields"]?.split(",")?.toSet()

            UserService.getMatching { Users.username eq username }.fold(
                success = {
                    ArticleService.getMatching { Articles.createdBy eq it.single().uuid }.fold(
                        success = { articles ->
                            try {
                                selectedPropertyNames?.let { props ->

                                    call.respond(articles.map { it.slice(props) })

                                } ?: call.respond(articles.map { it.sanitize() })
                            } catch (bruhMoment: Service.Exception) {
                                call.respondExceptionMessage(bruhMoment)
                            }
                        },
                        failure = { call.respondExceptionMessage(it) }
                    )
                },
                failure = { call.respondExceptionMessage(it) }
            )
        }

        delete("/{uuid}") {
            deleteWithId (
                fetch = ArticleService::get,
                delete = ArticleService::delete,
                authPredicate = { user, article -> article.createdBy == user },
                doAfter = { id ->
                    Typesense.deleteResource<Article>(id.toUUID())
                }
            )
        }

        patch("/{uuid}") {
            updateWithId (
                update = ArticleService::update,
                fetch = ArticleService::get,
                authPredicate = { user, article -> article.createdBy eqr user },
                doAfter = { replacement ->
                    Typesense.updateResource(replacement)
                }
            )
        }

        post("/") {
            createWithResource (
                create = ArticleService::add,
                authPredicate = { user, article -> article.createdBy eqr user },
                doAfter = { article ->
                   Typesense.uploadResource(article)
                }
            )
        }

        get("/search") {
            pipeline("q") { (query) ->
                val user = optional("byUser")?.toUUID()
                if (user != null) {
                    val userHandle = Article::class.cachedPropMap().ok()["createdBy"] ?: error("a")
                    call.respond(Typesense.search<Article>(query, mapOf(userHandle to user)).asSearchView())
                } else {
                    call.respond(Typesense.search<Article>(query).asSearchView())
                }
            }
        }

        get("_validations") {
            getValidations<Article>()
        }

        articleComments()

    }

}
