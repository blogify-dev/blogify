package blogify.backend.annotations.search

/**
 * Marks a property as being represented by it's UUID, and to be used as such in Typesense collections / documents
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class SearchByUUID
