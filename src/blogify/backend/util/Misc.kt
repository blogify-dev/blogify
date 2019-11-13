package blogify.backend.util

import io.ktor.http.ContentType

fun reason(text: String) = object { val reason = text }

fun <T, R> T.letCatchingOrNull(block: (T) -> R): R? {
    return try {
        block(this)
    } catch (e: Exception) {
        null
    }
}

infix fun ContentType.matches(other: ContentType) = this.match(other)

const val TYPESENSE_API_KEY = "Hu52dwsas2AdxdE"