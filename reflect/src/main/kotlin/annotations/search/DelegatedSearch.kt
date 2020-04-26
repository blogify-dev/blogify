package annotations.search

/**
 * Marks a property as having to be treated as the value of one of it's properties.
 * The return type of the property should have one and only one property marked with [DelegatedSearchReceiver].
 */
@Target(AnnotationTarget.TYPE)
@Retention(AnnotationRetention.RUNTIME)
annotation class DelegatedSearch
