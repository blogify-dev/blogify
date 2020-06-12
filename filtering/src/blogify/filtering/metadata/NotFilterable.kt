package blogify.filtering.metadata

/**
 * Marks a property of an Entity as not filterable. Whether or not a property is filterable can be accessed using
 * the following code : `<property>.descriptor.filtering.isFilterable`. A property is automatically unfilterable if it
 * is not of a compatible type.
 *
 * @author Benjozork
 */
@Target(AnnotationTarget.PROPERTY)
@Retention
annotation class NotFilterable
