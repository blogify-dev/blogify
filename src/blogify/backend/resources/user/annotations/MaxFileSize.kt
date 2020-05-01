package blogify.backend.resources.user.annotations

/**
 * Defines a size constraint for [static file][blogify.backend.resources.static.models.StaticFile] property.
 *
 * @author Benjozork
 */
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.PROPERTY)
annotation class MaxFileSize (
    val maxMegabyteSize: Double = 0.0
)
