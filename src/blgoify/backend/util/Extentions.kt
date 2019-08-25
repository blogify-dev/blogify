package blgoify.backend.util

import blgoify.backend.auth.encoder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

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