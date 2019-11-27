package blogify.backend.annotations

/**
 * Marks a property as being treated
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class SearchByUUID
