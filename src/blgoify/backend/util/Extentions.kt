package blgoify.backend.util

import blgoify.backend.auth.encoder

import java.util.*

import kotlin.concurrent.schedule

fun String.toUUID(): UUID = UUID.fromString(this)

fun String.hash(): String = encoder.encode(this)

/**
 * Please don't kill me.
 */
fun <T> T.letIn(time: Long, block: TimerTask.(T) -> Unit) {
    Timer().schedule(time) { block(this@letIn); this.cancel() }
}

/**
 * Encodes `,` separated list of categories provided by client to [Set]
 */
fun String.encodeToSet(): Set<String> = this.split(",").toSet()