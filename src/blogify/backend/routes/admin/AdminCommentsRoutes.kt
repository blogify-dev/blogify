package blogify.backend.routes.admin

import blogify.backend.routes.handling.deleteWithId
import blogify.backend.routes.handling.updateWithId
import blogify.backend.services.articles.CommentService
import io.ktor.routing.Route
import io.ktor.routing.delete
import io.ktor.routing.patch
import io.ktor.routing.route

fun Route.adminComments() {
    route("/comments") {

        delete("/{uuid}") {
            deleteWithId(
                fetch = CommentService::get,
                delete = CommentService::delete,
                authPredicate = { user, _ -> user.isAdmin }
            )
        }

        patch("/{uuid}") {
            updateWithId (
                update = CommentService::update,
                fetch = CommentService::get,
                authPredicate = { user, _ -> user.isAdmin }
            )
        }
    }
}

