package me.benjozork.routes

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*
import me.benjozork.resources.Comment
import me.benjozork.services.CommentService
import me.benjozork.util.toUUID

fun Route.comments() {
    route("/comments") {
        get("/article/{uuid}") {
            call.parameters["uuid"]?.toUUID()?.let {
                call.respond(CommentService.getForArticle(it))
            }
        }

        get("/user/{uuid}") {
            call.parameters["uuid"]?.toUUID()?.let {
                call.respond(CommentService.getByUser(it))
            }
        }

        post("/") {
            CommentService.add(call.receive())
            call.respond(HttpStatusCode.Created)
        }

        delete("/{uuid}") {
            val uuid = call.parameters["uuid"]?.toUUID()
            if (uuid == null) {
                call.respond(HttpStatusCode.NotFound)
            } else {
                CommentService.remove(uuid)
                call.respond(HttpStatusCode.OK)
            }
        }

        patch("/{uuid}") {
            val uuid = call.parameters["uuid"]?.toUUID()
            if (uuid == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@patch
            }

            val selected = CommentService.get(uuid)
            val replacement = call.receive<Comment>()
            if (selected == null) {
                call.respond(HttpStatusCode.NotFound)
            } else {
                CommentService.update(replacement)
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}