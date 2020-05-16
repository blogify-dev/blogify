package blogify.reflect.annotations

/**
 * Marks a property of a [blogify.reflect.models.Mapped] to not be included in a [propMap][blogify.reflect.models.PropMap]
 *
 * @author Benjozork
 */
@Suppress("ClassName")
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Hidden
