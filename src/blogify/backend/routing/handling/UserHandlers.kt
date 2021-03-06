package blogify.backend.routing.handling

import blogify.backend.annotations.BlogifyDsl
import blogify.backend.auth.handling.authenticated
import blogify.backend.pipelines.queryUuid
import blogify.backend.pipelines.wrapping.RequestContextFunction
import blogify.backend.resources.user.User
import blogify.backend.util.getOrPipelineError

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

    authenticated({ it.isAdmin && it != user }) {
        repository<User>().updateWithProperties (
            this,
            resource = user,
            data = mapOf (
                User::isAdmin to !user.isAdmin
            )
        ).getOrPipelineError()

        call.respond(HttpStatusCode.OK)
    }
}
