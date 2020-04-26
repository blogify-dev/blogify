package blogify.backend.routing

import blogify.backend.auth.handling.runAuthenticated
import blogify.backend.database.tables.Comments
import blogify.backend.database.handling.query
import blogify.backend.pipelines.*
import blogify.backend.pipelines.wrapping.ApplicationContext
import blogify.backend.resources.Comment
import blogify.backend.resources.models.eqr
import blogify.backend.pipelines.wrapping.RequestContext
import reflect.sanitize
import reflect.slice
import blogify.backend.routing.handling.*
import blogify.backend.util.*

import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.*

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

import org.jetbrains.exposed.sql.*

fun Route.makeArticleCommentRoutes(applicationContext: ApplicationContext) {

    route("/comments") {

        get("/") {
            requestContext(applicationContext) {
                fetchAllResources<Comment>()
            }
        }

        get("/{uuid}") {
            requestContext(applicationContext) {
                fetchResource<Comment>()
            }
        }

        get("/article/{uuid}") {
            requestContext(applicationContext) {
                val articleId by queryUuid

                fetchResourceListing<Comment>(
                    Comments.uuid,
                    SortOrder.ASC
                ) { Comments.article eq articleId and Comments.parentComment.isNull() }
            }
        }

        delete("/{uuid}") {
            requestContext(applicationContext) {
                deleteResource<Comment> (
                    authPredicate = { user, comment -> comment.commenter eqr user }
                )
            }
        }

        patch("/{uuid}") {
            requestContext(applicationContext) {
                updateResource<Comment> (
                    authPredicate = { user, comment -> comment.commenter eqr user }
                )
            }
        }

        post("/") {
            requestContext(applicationContext) {
                createResource<Comment> (
                    authPredicate = { user, comment -> comment.commenter eqr user }
                )
            }
        }

        fun expandCommentNodeAsync(request: RequestContext, comment: Comment, fields: Set<String>?, quantity: Int, page: Int, depth: Int): Deferred<Dto> {
            return GlobalScope.async { // We use async because inlining a suspending local function crashes the compiler
                val rootDto = fields?.let { comment.slice(it) } ?: comment.sanitize()
                val result = request.repository<Comment>().queryListing(request, { Comments.parentComment eq comment.uuid }, quantity, page, Comments.createdAt, SortOrder.DESC)
                    .getOrPipelineError(HttpStatusCode.InternalServerError, "error while expanding listing node")

                val children = if (depth > 0)
                    result.first.map { expandCommentNodeAsync(request, it, fields, quantity, page, depth - 1).await() } to result.second
                else (fields?.let {
                    result.first.map { it.slice(fields) }
                } ?: result.first.map { it.sanitize() }) to result.second

                return@async rootDto + ("children" to object { val data = children.first; val moreAvailable = children.second })
            }
        }

        get("/tree/comment/{uuid}") {
            requestContext(applicationContext) {
                val repo = repository<Comment>()

                val commentId by queryUuid

                val quantity = optionalParam("quantity")?.toIntOrNull()?.coerceAtMost(25) ?: 15
                val page     = optionalParam("page")?.toIntOrNull() ?: 0
                val depth    = optionalParam("depth")?.toIntOrNull()?.coerceAtMost(5) ?: 4
                val fields   = optionalParam("fields")?.split(',')?.toSet()

                val root = repo.get(this, commentId)
                    .getOrPipelineError(HttpStatusCode.InternalServerError, "error while querying listing")

                val expandedComments = expandCommentNodeAsync(this, root, fields, quantity, page, depth - 1).await()

                call.respond(expandedComments)
            }
        }

        get("/tree/article/{uuid}") {
            requestContext(applicationContext) {
                val repo = repository<Comment>()

                val articleId by queryUuid

                val quantity = optionalParam("quantity")?.toIntOrNull()?.coerceAtMost(25) ?: 15
                val page     = optionalParam("page")?.toIntOrNull() ?: 0
                val depth    = optionalParam("depth")?.toIntOrNull()?.coerceAtMost(5) ?: 4
                val fields   = optionalParam("fields")?.split(',')?.toSet()

                val root = repo.queryListing(this, { (Comments.article eq articleId) and Comments.parentComment.isNull() }, quantity, page, Comments.createdAt, SortOrder.DESC)
                    .getOrPipelineError(HttpStatusCode.InternalServerError, "error while querying listing")

                val expandedComments = root.first.map { expandCommentNodeAsync(this, it, fields, quantity, 0, depth - 1).await() }

                call.respond(object { val data = expandedComments; val moreAvailable = root.second })
            }
        }

        val likes = Comments.Likes

        get("/{uuid}/like") {
            requestContext(applicationContext) {
                val id by queryUuid

                runAuthenticated { subject ->
                    val comment = obtainResource<Comment>(id)

                    val liked = query {
                        likes.select {
                            (likes.comment eq comment.uuid) and (likes.user eq subject.uuid) }.count().toInt()
                    }.getOrPipelineError() == 1

                    call.respond(liked)
                }
            }
        }

        post("/{uuid}/like") {

            requestContext(applicationContext) {
                val id by queryUuid

                runAuthenticated { subject ->
                    val commentToLike = obtainResource<Comment>(id)

                    // Figure whether the article was already liked by the user
                    val alreadyLiked = query {
                        likes.select {
                            (likes.comment eq commentToLike.uuid) and (likes.user eq subject.uuid) }.count().toInt()
                    }.getOrPipelineError() == 1

                    if (!alreadyLiked) { // Add a like if none were present
                        query {
                            likes.insert {
                                it[likes.comment] = commentToLike.uuid
                                it[likes.user]    = subject.uuid
                            }
                        }.getOrPipelineError(HttpStatusCode.InternalServerError, "couldn't like comment")

                        call.respond(HttpStatusCode.OK, reason("comment liked"))
                    } else { // Remove an existing like if there was one
                        query {
                            likes.deleteWhere {
                                (likes.comment eq commentToLike.uuid) and (likes.user eq subject.uuid)
                            }
                        }.getOrPipelineError(HttpStatusCode.InternalServerError, "couldn't unlike comment")

                        call.respond(HttpStatusCode.OK, reason("comment unliked"))
                    }
                }
            }

        }

    }

}
