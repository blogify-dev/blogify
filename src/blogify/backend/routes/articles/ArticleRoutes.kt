@file:Suppress("DuplicatedCode")

package blogify.backend.routes.articles

import blogify.backend.database.Articles
import blogify.backend.database.Comments
import blogify.backend.database.Users
import blogify.backend.resources.Article
import blogify.backend.resources.models.eqr
import blogify.backend.resources.reflect.sanitize
import blogify.backend.resources.reflect.slice
import blogify.backend.routes.handling.*
import blogify.backend.search.Typesense
import blogify.backend.search.ext.asSearchView
import blogify.backend.services.UserService
import blogify.backend.services.articles.ArticleService
import blogify.backend.services.articles.CommentService
import blogify.backend.services.models.Service
import blogify.backend.util.toUUID

import io.ktor.application.call
import io.ktor.routing.*
import io.ktor.response.respond

fun Route.articles() {

    route("/articles") {

        get("/") {
            fetchAll(ArticleService::getAll)
        }

        get("/{uuid}") {
            fetchWithId(ArticleService::get)
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
            val params = call.parameters
            val selectedPropertyNames = params["fields"]?.split(",")?.toSet() // fixme: don't ignore this
            params["q"]?.let { query ->
                call.respond(Typesense.search<Article>(query).asSearchView())
            }
        }

        get("_validations") {
            getValidations<Article>()
        }

        articleComments()

    }

}
