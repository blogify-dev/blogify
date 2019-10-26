package blogify.backend.auth.handling

import blogify.backend.auth.jwt.validateJwt

import io.ktor.application.call
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.request.header
import io.ktor.response.respond

import blogify.backend.resources.User
import blogify.backend.routes.handling.CallPipeLineFunction
import blogify.backend.routes.handling.CallPipeline
import blogify.backend.annotations.BlogifyDsl
import blogify.backend.util.reason


/**
 * Represents a predicate applied on a [user][User].
 */
typealias UserAuthPredicate = suspend (user: User) -> Boolean

/**
 * Validates a token by making sure it authenticates a certain [User].
 *
 * @param mustBe the mustBe to whom the token must belong to
 */
fun isUser(mustBe: User): UserAuthPredicate = { user ->
        mustBe == user
}

/**
 * Allows to wrap a call handler into a block that takes care of authentication using a given [predicate][UserAuthPredicate].
 *
 * For example, using [isUser] as a [predicate][UserAuthPredicate] will result in the block only being
 * executed if the provided [user][User] matches the authenticating user.
 *
 * @param predicate the predicate used as a check for authentication
 * @param block     the call handling block that is run if the check succeeds
 */
@BlogifyDsl
suspend fun CallPipeline.runAuthenticated(predicate: UserAuthPredicate, block: CallPipeLineFunction) {
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

    validateJwt(call, token).fold (
        success = { u ->
            if (predicate.invoke(u)) { // Check token against predicate
                block.invoke(this, Unit)
            } else call.respond(HttpStatusCode.Forbidden)
        }, failure = { ex ->
            call.respond(HttpStatusCode.Forbidden, reason("invalid token - ${ex.javaClass.simpleName}"))
        }
    )

}
