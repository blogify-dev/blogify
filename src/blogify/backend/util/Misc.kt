package blogify.backend.util

fun reason(text: String) = object { val reason = text }

fun Byte.hex(): String {
    val raw = this.toInt().toString(16).toUpperCase()
    return if (raw.length == 1) "0$raw" else raw
}
