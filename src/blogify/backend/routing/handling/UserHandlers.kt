package blogify.backend.routing.handling

import blogify.backend.annotations.BlogifyDsl
import blogify.backend.pipelines.authenticate
import blogify.backend.pipelines.queryUuid
import blogify.backend.pipelines.wrapping.RequestContextFunction
import blogify.backend.resources.user.User
import blogify.backend.util.getOrPipelineError
import blogify.reflect.extensions.okHandle

import io.ktor.http.HttpStatusCode
import io.ktor.response.respond

/**
 * Request handler for making a user an admin
 */
@BlogifyDsl
val toggleUserAdmin: RequestContextFunction<Unit> = {
    val id by queryUuid

    val user = repository<User>().get(this, id)
        .getOrPipelineError(HttpStatusCode.NotFound)

    authenticate({ it.isAdmin && it != user }) {
        repository<User>().update(this, user, mapOf(User::isAdmin.okHandle!! to !user.isAdmin))
            .getOrPipelineError()

        call.respond(HttpStatusCode.OK)
    }

}
