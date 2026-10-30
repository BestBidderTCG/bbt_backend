package com.bestbidder.infrastructure.config

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey

@Service
class JwtService(
    @Value("\${jwt.secret}") secret: String,
    @Value("\${jwt.access-token-expiration-ms}") private val accessExpMs: Long,
) {
    private val key: SecretKey = Keys.hmacShaKeyFor(secret.toByteArray())

    fun generateAccessToken(
        userId: UUID,
        username: String,
    ): String =
        Jwts.builder()
            .subject(userId.toString())
            .claim("username", username)
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + accessExpMs))
            .signWith(key)
            .compact()

    fun parseAccessToken(token: String): ParsedAccessToken? =
        try {
            val claims =
                Jwts.parser().verifyWith(key).build()
                    .parseSignedClaims(token).payload
            ParsedAccessToken(
                userId = UUID.fromString(claims.subject),
                username = claims["username"] as String,
            )
        } catch (e: Exception) {
            null
        }
}

data class ParsedAccessToken(val userId: UUID, val username: String)
