package blgoify.backend.util

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.util.*

fun String.toUUID(): UUID = UUID.fromString(this)

fun String.hash(): String = BCryptPasswordEncoder(12).encode(this)