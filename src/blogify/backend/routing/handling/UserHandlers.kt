package blogify.backend.routing.handling

import blogify.backend.annotations.BlogifyDsl
import blogify.backend.auth.handling.authenticated
import blogify.backend.pipelines.queryUuid
import blogify.backend.pipelines.wrapping.RequestContextFunction
import blogify.backend.resources.user.User
import blogify.backend.util.getOr404OrPipelineError
import blogify.backend.util.getOrPipelineError

import io.ktor.http.HttpStatusCode
import io.ktor.response.respond

/**
 * Request handler for making a user an admin
 */
@BlogifyDsl
@ExperimentalStdlibApi
val toggleUserAdmin: RequestContextFunction<Unit> = {
    val id by queryUuid

    val user = repository<User>().obtain(id)
        .getOr404OrPipelineError()

    authenticated({ it.isAdmin && it != user }) {
        repository<User>().updateWithProperties (
            entity = user,
            data = mapOf (
                User::isAdmin to !user.isAdmin
            )
        ).getOrPipelineError()

        call.respond(HttpStatusCode.OK)
    }
}
