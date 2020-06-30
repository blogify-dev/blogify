package blogify.util

/**
 * Returns the content of an environment variable, or `null` if it's empty / non-existent
 *
 * @author Benjozork
 */
fun env(name: String) = System.getenv(name).takeIf { it?.isNotBlank() ?: false }
