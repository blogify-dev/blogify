package blogify.backend.annotations.search

/**
 * Marks a property as being the default sorting field of a model class.
 * If it doesn't appear on any fields, a fake sorting value is generated
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class SearchDefaultSort
