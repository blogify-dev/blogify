package blogify.backend.util

import io.ktor.http.ContentType

fun reason(text: String) = object { val reason = text }
fun reasons(vararg texts: String) = object { val reasons = texts }

infix fun ContentType.matches(other: ContentType) = this.match(other)
infix fun ContentType.matches(other: String) = this.match(other)

