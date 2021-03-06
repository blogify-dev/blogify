package blogify.reflect.annotations

/**
 * Marks a property of a [blogify.backend.resources.models.Resource] to *never* be consumed by [blogify.backend.routes.handling.slice] or [blogify.backend.routes.handling.sanitize]
 *
 * @author Benjozork
 */
@Suppress("ClassName")
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Hidden
