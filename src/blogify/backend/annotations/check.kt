package blogify.backend.annotations

@Suppress("ClassName")
@Target(AnnotationTarget.TYPE)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class check(val check: String)