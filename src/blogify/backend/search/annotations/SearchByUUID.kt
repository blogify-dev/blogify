package blogify.backend.search.annotations

/**
 * Marks a property as being represented by it's UUID, and to be used as such in Typesense collections / documents
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class SearchByUUID
