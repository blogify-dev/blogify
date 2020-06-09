package blogify.reflect.entity.annotations

/**
 * Marks a property of a [blogify.reflect.entity.Entity] as not updatable
 *
 * @author Benjozork
 */
@Suppress("ClassName")
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class NotUpdatable
