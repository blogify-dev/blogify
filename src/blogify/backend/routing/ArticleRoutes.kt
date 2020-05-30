@file:Suppress("DuplicatedCode")

package blogify.backend.routing

import blogify.backend.database.handling.query
import blogify.backend.database.tables.Articles
import blogify.backend.pipelines.*
import blogify.backend.pipelines.wrapping.ApplicationContext
import blogify.backend.resources.Article
import blogify.backend.resources.user.User
import blogify.backend.routing.handling.flipArticleLike
import blogify.backend.routing.handling.flipArticlePin
import blogify.backend.routing.handling.getArticleLikeStatus
import blogify.backend.routing.handling.*
import blogify.backend.util.getOr404OrPipelineError
import blogify.backend.util.toUUID
import blogify.backend.util.toUUIDOrNull
import blogify.reflect.sanitize
import epgx.functions.`@@`
import epgx.functions.toTsQuery
import epgx.functions.webSearchToTsQuery
import io.ktor.http.HttpStatusCode

import io.ktor.routing.*
import io.ktor.response.respond

import org.jetbrains.exposed.sql.*

fun Route.makeArticleRoutes(applicationContext: ApplicationContext) {

    route("/articles") {

        get("/") {
            requestContext(applicationContext) {
                fetchResourceListing<Article> (
                    selectCondition = { Articles.isDraft eq false },
                    orderBy = Articles.isPinned,
                    sortOrder = SortOrder.DESC
                )
            }
        }

        get("/user/{uuid}") {
            requestContext(applicationContext) {
                val id by queryUuid

                fetchResourceListing<Article> (
                    selectCondition = { (Articles.createdBy eq id) and (Articles.isDraft eq false) },
                    orderBy = Articles.isPinned,
                    sortOrder = SortOrder.DESC
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
                val byUser = optionalParam("byUser")?.toUUIDOrNull()

                fetchResourceListing<Article> (
                    selectCondition = {
                        Articles.tsvector `@@` QueryParameter(query, TextColumnType()).webSearchToTsQuery("english") and
                        if (byUser == null) { Op.TRUE } else { Articles.createdBy eq byUser }
                    },
                    orderBy = Articles.createdAt,
                    sortOrder = SortOrder.DESC
                )
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
