package blogify.util

import com.andreapivetta.kolor.red
import kotlin.system.exitProcess

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

    if (env("BLOGIFY_ENABLE_ASSERTIONS")
            ?.trim() == "1") exitProcess(155)
    else error("fatal: exception thrown due to failed assertion".red())
}
