package blogify.common.util

import java.util.*

fun String.toUUID(): UUID = UUID.fromString(this)

fun String.toUUIDOrNull(): UUID? = letCatchingOrNull { UUID.fromString(this) }

fun UUID.short(): String = this.toString().takeLast(8)
