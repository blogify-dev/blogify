package blogify.backend.routing

import blogify.backend.auth.handling.runAuthenticated
import blogify.backend.database.Comments
import blogify.backend.database.handling.query
import blogify.backend.pipelines.wrapping.ApplicationContext
import blogify.backend.resources.Comment
import blogify.backend.resources.models.eqr
import blogify.backend.pipelines.obtainResource
import blogify.backend.pipelines.optionalParam
import blogify.backend.pipelines.param
import blogify.backend.pipelines.requestContext
import blogify.backend.routing.handling.*
import blogify.backend.util.expandCommentNode
import blogify.backend.util.getOrPipelineError
import blogify.backend.util.reason
import blogify.backend.util.toUUID

import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.*
import org.jetbrains.exposed.sql.*

fun Route.articleComments() {

    route("/comments") {

        get("/{uuid}") {
            requestContext {
                fetchResource<Comment>()
            }
        }

        get("/article/{uuid}") {
            requestContext {
                val articleId = param("uuid").toUUID()
                fetchResourceListing<Comment>(
                    Comments.uuid,
                    SortOrder.ASC
                ) { Comments.article eq articleId and Comments.parentComment.isNull() }
            }
        }

        delete("/{uuid}") {
            requestContext {
                deleteResource<Comment> (
                    authPredicate = { user, comment -> comment.commenter eqr user }
                )
            }
        }

        patch("/{uuid}") {
            requestContext {
                updateResource<Comment> (
                    authPredicate = { user, comment -> comment.commenter eqr user }
                )
            }
        }

        post("/") {
            requestContext {
                createResource<Comment> (
                    authPredicate = { user, comment -> comment.commenter eqr user }
                )
            }
        }

        get("/tree/{uuid}") {
            requestContext {
                val repo = repository<Comment>()

                val id      = param("uuid").toUUID()
                val fetched = repo.get(this, id)
                val depth   = optionalParam("depth")?.toIntOrNull() ?: 5

                call.respond(expandCommentNode(this, repository = repo, rootNode = fetched.get(), depth = depth))
            }
        }

        val likes = Comments.Likes

        get("/{uuid}/like") {
            requestContext {
                val id = param("uuid")

                runAuthenticated { subject ->
                    val comment = this.obtainResource<Comment>(id.toUUID())

                    val liked = query {
                        likes.select {
                            (likes.comment eq comment.uuid) and (likes.user eq subject.uuid) }.count().toInt()
                    }.getOrPipelineError() == 1

                    call.respond(liked)
                }
            }
        }

        post("/{uuid}/like") {

            requestContext {
                val id = param("uuid")

                runAuthenticated { subject ->
                    val commentToLike = repository<Comment>().get(this, id.toUUID())
                        .getOrPipelineError(HttpStatusCode.NotFound, "couldn't fetch comment")

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
