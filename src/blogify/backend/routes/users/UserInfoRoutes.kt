package blogify.backend.routes.users

import blogify.backend.routes.handling.handleResourceFetch
import blogify.backend.services.UserService
import io.ktor.routing.Route
import io.ktor.routing.get

fun Route.userInfo() {

    get("/info/{uuid}") {
        handleResourceFetch(UserService::get, transform = { it.info().get() })
    }

}