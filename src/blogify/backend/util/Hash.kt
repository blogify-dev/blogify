package blogify.backend.util

import blogify.backend.auth.encoder

fun String.hash(): String = encoder.encode(this)
