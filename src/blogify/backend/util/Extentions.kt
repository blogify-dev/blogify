package blogify.backend.util

import blogify.backend.auth.encoder

import java.util.*

fun String.toUUID(): UUID = UUID.fromString(this)

fun UUID.short(): String = this.toString().takeLast(8)

fun String.hash(): String = encoder.encode(this)

