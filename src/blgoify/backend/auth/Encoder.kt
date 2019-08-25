package blgoify.backend.auth

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

/**
 * This defines the number of `bcrypt` log rounds to use
 */
private const val BCRYPT_LOG_ROUNDS = 12

val encoder = BCryptPasswordEncoder(BCRYPT_LOG_ROUNDS)