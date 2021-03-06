package blogify.backend.annotations

/**
 * Marks a [blogify.backend.resources.static.models.StaticFile] type as only accepting a
 * certain [io.ktor.http.ContentType]. Is queried when a file is uploaded to a handle in a [blogify.backend.resources.models.Resource].
 *
 * Omitting this annotations is equivalent to setting content type `*`.
 *
 * @author Benjozork
 */
@Suppress("ClassName")
@Target(AnnotationTarget.TYPE)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class type(val contentType: String)
