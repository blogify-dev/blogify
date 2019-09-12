package blogify.backend.auth.jwt

import blogify.backend.resources.User

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys

private val keyPair = Keys.keyPairFor(SignatureAlgorithm.ES512)

fun generateJWT(user: User) = Jwts
    .builder()
    .setSubject(user.uuid.toString())
    .setIssuer("blogify")
    .signWith(keyPair.private).compact()