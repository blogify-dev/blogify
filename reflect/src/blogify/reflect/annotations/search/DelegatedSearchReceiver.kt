package blogify.reflect.annotations.search

/**
 * Marks a property of a class as being the value by which it is represented when present in a class property marked with
 * [DelegatedSearch]
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class DelegatedSearchReceiver
