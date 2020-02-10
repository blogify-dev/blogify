package blogify.backend.routing

import blogify.backend.auth.handling.runAuthenticated
import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.*

import blogify.backend.database.Comments
import blogify.backend.database.handling.query
import blogify.backend.pipelines.ApplicationContext
import blogify.backend.resources.Comment
import blogify.backend.resources.models.eqr
import blogify.backend.routing.handling.createResource
import blogify.backend.routing.handling.deleteResource
import blogify.backend.routing.handling.fetchAllResources
import blogify.backend.routing.handling.fetchAllWithId
import blogify.backend.routing.handling.fetchResource
import blogify.backend.routing.handling.updateResource
import blogify.backend.routing.pipelines.obtainResource
import blogify.backend.routing.pipelines.param
import blogify.backend.routing.pipelines.request
import blogify.backend.routing.pipelines.wrapRequest
import blogify.backend.services.CommentRepository
import blogify.backend.util.expandCommentNode
import blogify.backend.util.getOrPipelineError
import blogify.backend.util.reason
import blogify.backend.util.toUUID
import io.ktor.http.HttpStatusCode

import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select

fun Route.articleComments(applicationContext: ApplicationContext) {

    route("/comments") {

        get("/") {
            wrapRequest(applicationContext) {
                fetchAllResources<Comment>()
            }
        }

        get("/{uuid}") {
            wrapRequest(applicationContext) {
                fetchResource<Comment>()
            }
        }

        get("/article/{uuid}") {
            wrapRequest(applicationContext) {
                fetchAllWithId(fetch = { articleId ->
                    CommentRepository.getMatching(call) { Comments.article eq articleId and Comments.parentComment.isNull() }
                })
            }
        }

        delete("/{uuid}") {
            wrapRequest(applicationContext) {
                deleteResource<Comment> (
                    authPredicate = { user, comment -> comment.commenter eqr user }
                )
            }
        }

        patch("/{uuid}") {
            wrapRequest(applicationContext) {
                updateResource<Comment> (
                    authPredicate = { user, comment -> comment.commenter eqr user }
                )
            }
        }

        post("/") {
            wrapRequest(applicationContext) {
                createResource<Comment> (
                    authPredicate = { user, comment -> comment.commenter eqr user }
                )
            }
        }

        get("/tree/{uuid}") {
            val fetched = CommentRepository.get(call, call.parameters["uuid"]!!.toUUID())

            val depth = call.parameters["depth"]?.toInt() ?: 5

            call.respond(expandCommentNode(call, fetched.get(), depth = depth))
        }

        val likes = Comments.Likes

        get("/{uuid}/like") {
            wrapRequest(applicationContext) {
                request {

                    val id = param("uuid")

                    runAuthenticated {
                        val comment = this.obtainResource<Comment>(id.toUUID())

                        val liked = query {
                            likes.select {
                                (likes.comment eq comment.uuid) and (likes.user eq subject.uuid) }.count()
                        }.getOrPipelineError() == 1;

                        call.respond(liked)
                    }
                }
            }
        }

        post("/{uuid}/like") {

            wrapRequest(applicationContext) {
                request {

                    val id = param("uuid")

                    runAuthenticated {
                        val commentToLike = CommentRepository.get(call, id.toUUID())
                            .getOrPipelineError(HttpStatusCode.NotFound, "couldn't fetch comment")

                        // Figure whether the article was already liked by the user
                        val alreadyLiked = query {
                            likes.select {
                                (likes.comment eq commentToLike.uuid) and (likes.user eq subject.uuid) }.count()
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

}
