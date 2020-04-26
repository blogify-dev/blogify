package annotations

/**
 * Marks a property of a [blogify.backend.resources.models.Resource] to not be sent in a DTO.
 *
 * @author Benjozork
 */
@Suppress("ClassName")
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Undisplayed
