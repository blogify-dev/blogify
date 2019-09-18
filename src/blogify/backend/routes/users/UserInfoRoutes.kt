package blogify.backend.routes.users

import io.ktor.routing.Route
import io.ktor.routing.get

import blogify.backend.routes.handling.fetchWithIdAndRespond
import blogify.backend.services.UserService

fun Route.userInfo() {

    get("/info/{uuid}") {
        fetchWithIdAndRespond(UserService::get, transform = { it.info().get() })
    }

}
