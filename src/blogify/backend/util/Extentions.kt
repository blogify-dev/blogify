package blogify.backend.util

import blogify.backend.auth.encoder

import java.util.*

import kotlin.concurrent.schedule

fun String.toUUID(): UUID = UUID.fromString(this)

fun UUID.short(): String = this.toString().takeLast(8)

fun String.hash(): String = encoder.encode(this)

/**
 * Please don't kill me.
 */
fun <T> T.letIn(time: Long, block: TimerTask.(T) -> Unit) {
    Timer().schedule(time) { block(this@letIn); this.cancel() }
}
