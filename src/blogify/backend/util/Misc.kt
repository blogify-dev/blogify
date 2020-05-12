package blogify.backend.util

import com.andreapivetta.kolor.red
import io.ktor.http.ContentType
import kotlin.system.exitProcess

fun reason(text: String) = object { val reason = text }
fun reasons(vararg texts: String) = object { val reasons = texts }

fun <T, R> T.letCatchingOrNull(block: (T) -> R): R? {
    return try {
        block(this)
    } catch (e: Exception) {
        null
    }
}

infix fun ContentType.matches(other: ContentType) = this.match(other)
infix fun ContentType.matches(other: String) = this.match(other)

/**
 * Returns the content of an environment variable, or `null` if it's empty / non-existent
 *
 * @author Benjozork
 */
fun env(name: String) = System.getenv(name).takeIf { it?.isNotBlank() ?: false }

/**
 * If this property is evaluated and the `BLOGIFY_ENABLE_ASSERTIONS` env variable is set to 1, the server immediately dies.
 * Should be used as the right-hand side of Elvis expressions, like in the following :
 * `shouldNeverReturnNull() ?: never`. Useful for representing states that should never be reached.
 *
 * @author Benjozork
 */
val never: Nothing get() {
    println("### FATAL ERROR : assertion failed - shutting down system! stack-trace : ###".red())

    Exception().printStackTrace()

    if (env("BLOGIFY_ENABLE_ASSERTIONS")?.trim() == "1") exitProcess(155)
    else error("fatal: exception thrown due to failed assertion".red())
}
