package blgoify.backend.routes.handling.auth

import io.ktor.application.call
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.request.header
import io.ktor.response.respond

import blgoify.backend.resources.User
import blgoify.backend.routes.handling.CallPipeLineFunction
import blgoify.backend.routes.handling.CallPipeline
import blgoify.backend.routes.validTokens
import blgoify.backend.util.BlogifyDsl
import blgoify.backend.util.toUUID

/**
 * Represents a predicate to be applied to a token
 */
typealias AuthenticationPredicate = (token: String) -> Boolean

/**
 * Validates a token by making sure it authenticates a certain [User].
 *
 * @param user the user to whom the token must belong to
 */
fun isUser(user: User): AuthenticationPredicate = { token ->
    validTokens.filterKeys { it.toUUID() == user.uuid }.values.any { it == token }
}

@BlogifyDsl
suspend fun CallPipeline.authenticatedBy(predicate: AuthenticationPredicate, block: CallPipeLineFunction) {
    val header = call.request.header(HttpHeaders.Authorization) ?: run {
        call.respond(HttpStatusCode.BadRequest) // Header is missing
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