package com.bestbidder.infrastructure.security

import com.bestbidder.domain.user.exceptions.InvalidRefreshTokenException
import com.bestbidder.usecase.port.RefreshTokenRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.LocalDateTime
import java.util.Base64
import java.util.UUID

@Service
class RefreshTokenService(
    private val repository: RefreshTokenRepository,
    @Value("\${jwt.refresh-token-expiration-days}") private val expirationDays: Long,
) {
    fun generate(userId: UUID): String {
        val bytes = ByteArray(64).also { SecureRandom().nextBytes(it) }
        val raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
        repository.save(
            userId = userId,
            tokenHash = hash(raw),
            expiresAt = LocalDateTime.now().plusDays(expirationDays),
        )
        return raw
    }

    fun validateAndConsume(token: String): UUID {
        val h = hash(token)
        val stored = repository.findByHash(h) ?: throw InvalidRefreshTokenException()
        repository.deleteByHash(h)
        if (stored.expiresAt.isBefore(LocalDateTime.now())) {
            throw InvalidRefreshTokenException()
        }
        return stored.userId
    }

    fun revoke(token: String) = repository.deleteByHash(hash(token))

    fun revokeAll(userId: UUID) = repository.deleteAllByUserId(userId)

    private fun hash(input: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(input.toByteArray())
            .joinToString("") { "%02x".format(it) }
}
