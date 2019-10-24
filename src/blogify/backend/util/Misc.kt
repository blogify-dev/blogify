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

fun Byte.hex(): String {
    val raw = this.toInt().toString(16).toUpperCase()
    return if (raw.length == 1) "0$raw" else raw
}
