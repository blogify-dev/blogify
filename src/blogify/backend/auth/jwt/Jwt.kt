package blogify.backend.auth.jwt

import blogify.backend.resources.User

import com.andreapivetta.kolor.red

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory

import java.util.Calendar
import java.util.Date

private val keyPair = Keys.keyPairFor(SignatureAlgorithm.ES512)

private val logger = LoggerFactory.getLogger("blogify-auth-token")

fun generateJWT(user: User) = Jwts
    .builder()
    .setSubject(user.uuid.toString())
    .setIssuer("blogify")
    .apply {
        val cal = Calendar.getInstance()

        cal.time = Date()
        cal.add(Calendar.DAY_OF_MONTH, +15)

        setExpiration(cal.time)
    }
    .signWith(keyPair.private).compact().also {
        logger.debug("created token for user with id {${user.uuid.toString().take(8)}...}")
    }

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
    } catch(e: Exception) {
        logger.debug("${"invalid token attempted".red()} - ${e.javaClass.simpleName.takeLastWhile { it != '.' }}")
        e.printStackTrace()
        return false
    }

    return true
}