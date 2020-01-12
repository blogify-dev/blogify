package blogify.backend.util

import io.ktor.http.ContentType

fun reason(text: String) = object { val reason = text }
fun reasons(vararg texts: String) = object { val reasons = texts }

fun <T, R> T.letCatchingOrNull(block: (T) -> R): R? {
    return try {
        block(this)
    } catch (e: Exception) {
        null
    }
}

infix fun ContentType.matches(other: ContentType) = this.match(other)
infix fun ContentType.matches(other: String) = this.match(other)

/**
 * Returns the content of an environment variable, or `null` if it's empty / non-existent
 *
 * @author Benjozork
 */
fun env(name: String) = System.getenv(name).takeIf { it?.isNotBlank() ?: false }
