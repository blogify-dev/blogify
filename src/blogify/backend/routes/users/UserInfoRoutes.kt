package blogify.backend.routes.users

import io.ktor.routing.Route
import io.ktor.routing.get

import blogify.backend.routes.handling.handleResourceFetch
import blogify.backend.services.UserService

fun Route.userInfo() {

    get("/info/{uuid}") {
        handleResourceFetch(UserService::get, transform = { it.info().get() })
    }

}
