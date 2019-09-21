package blogify.backend.auth.jwt

import blogify.backend.resources.User

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys

import java.util.Date

private val keyPair = Keys.keyPairFor(SignatureAlgorithm.ES512)

fun generateJWT(user: User) = Jwts
    .builder()
    .setSubject(user.uuid.toString())
    .setIssuer("blogify")
    .setIssuedAt(Date())
    .signWith(keyPair.private).compact()

/**
* Validates a JWT, returning a [User] if that token authenticates a user, or `null` if the token is invalid
 */
fun validateJwt(token: String): Boolean {
    try {
        Jwts
            .parser()
            .setSigningKey(keyPair.public)
            .requireIssuer("blogify")
            .setAllowedClockSkewSeconds(1)
            .parseClaimsJws(token)
            .body.takeIf { it.expiration.also { e -> println(e) } < Date() } ?: error("token expired")
    } catch(e: Exception) {
        return false
    }

    return true
}