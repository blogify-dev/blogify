package me.benjozork.routes

import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route

import me.benjozork.services.UserService

fun Route.users() {
    route("/users") {

        // Get all users

        get("/") {
            call.respond(UserService.getAll())
        }

    }

}
