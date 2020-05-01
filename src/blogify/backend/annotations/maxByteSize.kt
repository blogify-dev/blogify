package blogify.backend.annotations

/**
 * Marks a [blogify.backend.resources.static.models.StaticFile] type as only accepting a
 * file of a certain size. Is queried when a file is uploaded to a handle in a [blogify.backend.resources.models.Resource].
 *
 * Omitting this annotations is equivalent to not setting a file size limit
 *
 * @author Benjozork
 */
@Suppress("ClassName")
@Target(AnnotationTarget.TYPE)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class maxByteSize(val value: Long)
