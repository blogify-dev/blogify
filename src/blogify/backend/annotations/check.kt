package blogify.backend.annotations

import org.intellij.lang.annotations.Language

@Suppress("ClassName")
@Target(AnnotationTarget.TYPE)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class check (
    @Language(value = "RegExp") val pattern: String
)
