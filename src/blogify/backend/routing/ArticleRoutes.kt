@file:Suppress("DuplicatedCode")

package blogify.backend.routing

import blogify.backend.database.tables.Articles
import blogify.backend.pipelines.*
import blogify.backend.pipelines.wrapping.ApplicationContext
import blogify.backend.resources.Article
import blogify.backend.routing.handling.flipArticleLike
import blogify.backend.routing.handling.flipArticlePin
import blogify.backend.routing.handling.getArticleLikeStatus
import blogify.backend.search.Typesense
import blogify.backend.search.ext.asSearchView
import blogify.reflect.extensions.okHandle
import blogify.backend.routing.handling.*
import blogify.backend.util.*

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
                    authPredicate = { user, article -> article.createdBy == user }
                )
            }
        }

        post("/") {
            requestContext(applicationContext) {
                createResource<Article> (
                    authPredicate = { user, article -> article.createdBy == user }
                )
            }
        }

        post("/{uuid}/pin") {
            requestContext(applicationContext, flipArticlePin)
        }

        route("/{uuid}/like") {
            get  { requestContext(applicationContext, getArticleLikeStatus) }
            post { requestContext(applicationContext, flipArticleLike) }
        }

        get("/search") {
            requestContext(applicationContext) {
                val query = param("q")
                val user = optionalParam("byUser")?.toUUID()

                if (user != null) {
                    val userHandle = Article::createdBy.okHandle ?: never
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
