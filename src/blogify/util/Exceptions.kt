package blogify.util

fun <T, R> T.letCatchingOrNull(block: (T) -> R): R? {
    return try {
        block(this)
    } catch (e: Exception) {
        null
    }
}
