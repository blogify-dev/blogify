package blogify.backend.auth.jwt

import blogify.backend.pipelines.wrapping.RequestContext
import blogify.backend.resources.user.User
import blogify.backend.resources.models.Resource.ObjectResolver.FakeRequestContext
import blogify.backend.util.Sr
import blogify.backend.util.short
import blogify.backend.util.toUUID

import com.github.kittinunf.result.coroutines.SuspendableResult

import com.andreapivetta.kolor.green
import com.andreapivetta.kolor.red

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys

import org.slf4j.LoggerFactory

import java.util.Calendar
import java.util.Date

private val keyPair = Keys.keyPairFor(SignatureAlgorithm.ES512)

private val logger = LoggerFactory.getLogger("blogify-auth-token")

/**
 * Creates a [Jws] for the specific [user].
 */
fun generateJWT(user: User) = Jwts
    .builder()
    .setSubject(user.uuid.toString())
    .setIssuer("blogify")
    .apply {
        val cal = Calendar.getInstance()

        cal.time = Date()
        cal.add(Calendar.DAY_OF_MONTH, +7)

        setExpiration(cal.time)
    }
    .signWith(keyPair.private).compact().also {
        logger.debug("${"created token for user with id".green()} {${user.uuid.short()}...}")
    }

/**
* Validates a JWT, returning a [Sr] accordingly
 */
suspend fun validateJwt(requestContext: RequestContext = FakeRequestContext, token: String): Sr<User> {
    var jwsClaims: Jws<Claims>? = null

    try {
        jwsClaims = Jwts
            .parser()
            .setSigningKey(keyPair.public)
            .requireIssuer("blogify")
            .setAllowedClockSkewSeconds(1)
            .parseClaimsJws(token)
    } catch(e: JwtException) {
        logger.debug("${"invalid token attempted".red()} - ${e.javaClass.simpleName.takeLastWhile { it != '.' }}")
        println(e.message?.red())
        return SuspendableResult.error(e)
    } catch (e: Exception) {
        logger.debug("${"unknown exception during token validation -".red()} - ${e.javaClass.simpleName.takeLastWhile { it != '.' }}")
        e.printStackTrace()
    }

    val user = requestContext.repository<User>().get(requestContext, jwsClaims?.body?.subject?.toUUID() ?: error("malformed uuid in jwt"))
    logger.debug("got valid JWT for user {${user.get().uuid.short()}...}".green())

    return user
}
