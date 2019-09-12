package blogify.backend.auth.handling

import io.ktor.application.call
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.request.header
import io.ktor.response.respond

import blogify.backend.resources.User
import blogify.backend.routes.handling.CallPipeLineFunction
import blogify.backend.routes.handling.CallPipeline
import blogify.backend.routes.validTokens
import blogify.backend.util.BlogifyDsl
import blogify.backend.util.foldForOne

/**
 * Represents a predicate applied on a token.
 */
typealias AuthPredicate = suspend (token: String) -> Boolean

/**
 * Represents a predicate applied on a [user][User].
 */
typealias UserAuthPredicate = suspend (user: User) -> Boolean

/**
 * Wraps a [UserAuthPredicate] in an [AuthPredicate], allowing for methods to simply
 * check against a [User] object instead of a token when using [authenticatedBy].
 *
 * @param token         the token that should be used to provide the [User] object to [nextPredicate]
 * @param nextPredicate the [predicate][UserAuthPredicate] that should be run on a [user][User]
 *
 * @return `false` if no users match the given [token] or if the wrapped predicate returns `false`.
 */
@BlogifyDsl
private suspend fun predicateOnUser(token: String, nextPredicate: UserAuthPredicate): Boolean {
    return validTokens
        .filterValues { it == token }
        .keys.foldForOne (
            one      = { u -> nextPredicate.invoke(u); true },
            multiple = { error("multiple user for token") }, // This shouldn't be an error
            none     = { false }
        )
}

/**
 * Validates a token by making sure it authenticates a certain [User].
 *
 * @param user the user to whom the token must belong to
 */
fun isUser(user: User): AuthPredicate = { token ->
    predicateOnUser(token) {
        it == user
    }
}

/**
 * Allows to wrap a call handler into a block that takes care of authentication using a given [predicate][AuthPredicate].
 *
 * For example, using [isUser] as a [predicate][AuthPredicate] will result in the block only being
 * executed if the provided [user][User] matches the authenticating user.
 *
 * @param predicate the predicate used as a check for authentication
 * @param block     the call handling block that is run if the check succeeds
 */
@BlogifyDsl
suspend fun CallPipeline.authenticatedBy(predicate: AuthPredicate, block: CallPipeLineFunction) {
    val header = call.request.header(HttpHeaders.Authorization) ?: run {
        call.respond(HttpStatusCode.Unauthorized) // Header is missing
        return
    }

    val token = header // Header validity procedure
        .substringAfter("Bearer ", "none")
        .takeIf { it != "none" && it.length == 86 /* 512 bit base64 token length */ }?.let { token -> // Token is provided
            token // Propagate token value to token val
        } ?: run { call.respond(HttpStatusCode.BadRequest); return } // Token is invalid or missing

    if (predicate.invoke(token)) { // Check token against predicate
        block.invoke(this, Unit)
    } else call.respond(HttpStatusCode.Forbidden)

}