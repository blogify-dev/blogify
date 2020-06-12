@file:Suppress("DuplicatedCode")

package blogify.backend.routing

import blogify.common.util.toUUID
import blogify.reflect.extensions.okHandle
import blogify.backend.database.tables.Articles
import blogify.backend.pipelines.*
import blogify.backend.resources.Article
import blogify.backend.routing.handling.flipArticleLike
import blogify.backend.routing.handling.flipArticlePin
import blogify.backend.routing.handling.getArticleLikeStatus
import blogify.backend.search.Typesense
import blogify.backend.search.ext.asSearchView
import blogify.backend.routing.handling.*
import blogify.common.util.never

import io.ktor.routing.*
import io.ktor.response.respond

import org.jetbrains.exposed.sql.*

fun Route.makeArticleRoutes() {

    route("/articles") {

        get("/") {
            requestContext {
                fetchResourceListing<Article> (
                    selectCondition = { Articles.isDraft eq false },
                    orderBy = Articles.isPinned,
                    sortOrder = SortOrder.DESC
                )
            }
        }

        get("/user/{uuid}") {
            requestContext {
                val id by queryUuid

                fetchResourceListing<Article> (
                    selectCondition = { (Articles.createdBy eq id) and (Articles.isDraft eq false) },
                    orderBy = Articles.isPinned,
                    sortOrder = SortOrder.DESC
                )
            }
        }

        get("/{uuid}") {
            requestContext {
                fetchResource<Article>()
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
                    authPredicate = { user, article -> article.createdBy == user }
                )
            }
        }

        post("/") {
            requestContext {
                createResource<Article> (
                    authPredicate = { user, article -> article.createdBy == user }
                )
            }
        }

        post("/{uuid}/pin") {
            requestContext(flipArticlePin)
        }

        route("/{uuid}/like") {
            get  { requestContext(getArticleLikeStatus) }
            post { requestContext(flipArticleLike) }
        }

        get("/search") {
            requestContext {
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
            requestContext {
                getValidations<Article>()
            }
        }

        get("_metadata") {
            requestContext(getTypeMetadata(Article::class))
        }

        makeArticleCommentRoutes()

    }

}
