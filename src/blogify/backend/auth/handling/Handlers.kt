package blogify.backend.auth.handling

import blogify.backend.auth.jwt.validateJwt

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.request.header
import io.ktor.response.respond

import blogify.backend.resources.user.User
import blogify.backend.annotations.PipelinesDsl
import blogify.backend.pipelines.wrapping.RequestContext
import blogify.backend.pipelines.wrapping.RequestContextFunction
import blogify.backend.util.reason

/**
 * Represents a predicate applied on a [user][User].
 */
@ExperimentalStdlibApi
typealias UserAuthPredicate = suspend (user: User) -> Boolean

/**
 * Allows to wrap a call-handling block into a possible authentication.
 *
 * If [predicate] is null, authentication will be bypassed and therefore the block will be invoked anyway
 * with a null user as parameter.
 * If [predicate] is *not* null, authentication will be performed using the predicate and the block will be run
 * with the authenticated user as parameter.
 *
 * @param predicate the predicate used as a check for authentication
 * @param block     the call handling block that is run if the check succeeds
 *
 * @author Benjozork
 */
@Suppress("EXPERIMENTAL_API_USAGE")
@PipelinesDsl
@ExperimentalStdlibApi
suspend fun RequestContext.maybeAuthenticated (
    predicate: UserAuthPredicate? = { true },
    block: RequestContextFunction<User?>
) {
    if (predicate == null) {
        this.execute(block, null)
        return
    }

    val header = call.request.header(HttpHeaders.Authorization) ?: run {
        call.respond(HttpStatusCode.Unauthorized) // Header is missing
        return
    }

    val token = header // Header validity procedure
        .substringAfter("Bearer ", "none")

    if (token == "none") {
        call.respond(HttpStatusCode.BadRequest, reason("malforrmed token"))
        return
    }

    validateJwt(this, token).fold (
        success = { user ->
            if (predicate.invoke(user)) { // Check token against predicate
                this.execute(block, user)
            } else call.respond(HttpStatusCode.Forbidden)
        }, failure = { ex ->
            call.respond(HttpStatusCode.Forbidden, reason("invalid token - ${ex.javaClass.simpleName}"))
        }
    )

}

/**
 * Allows to wrap a call-handling block into authentication.
 *
 * If [predicate] is null, authentication will not be performed with the only condition being valid identity,
 * and the block will be run with null as parameter.
 * If [predicate] is *not* null, authentication will be performed using the predicate and the block will be run
 * with the authenticated user as parameter.
 *
 * @param predicate the predicate used as a check for authentication
 * @param block     the call handling block that is run if the check succeeds
 *
 * @author Benjozork
 */
@PipelinesDsl
@ExperimentalStdlibApi
suspend fun RequestContext.optionallyAuthenticated (
    predicate: UserAuthPredicate? = { true },
    block: RequestContextFunction<User?>
) {
    if (call.request.headers["Authorization"] != null) {
         maybeAuthenticated(predicate, block)
    } else {
        this.execute(block, null)
    }
}


/**
 * Allows to wrap a call-handling block into authentication.
 *
 * If [predicate] is null, authentication will be performed with the only condition being valid identity,
 * and the block will be run with the authenticated user as parameter.
 * If [predicate] is *not* null, authentication will be performed using the predicate and the block will be run
 * with the authenticated user as parameter.
 *
 * @param predicate the predicate used as a check for authentication
 * @param block     the call handling block that is run if the check succeeds
 *
 * @author Benjozork
 */
@PipelinesDsl
@ExperimentalStdlibApi
suspend fun RequestContext.authenticated (
    predicate: (suspend (User) -> Boolean) = { true },
    block: RequestContextFunction<User>
) = maybeAuthenticated(predicate, { block(it!!) })
