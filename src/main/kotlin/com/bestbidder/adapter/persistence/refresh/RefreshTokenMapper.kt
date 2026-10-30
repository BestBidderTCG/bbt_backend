package com.bestbidder.adapter.persistence.refresh

import com.bestbidder.usecase.port.RefreshTokenRepository
import com.bestbidder.usecase.port.StoredRefreshToken
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.UUID

@Component
class RefreshTokenMapper(
    private val jpa: JpaRefreshTokenRepository,
) : RefreshTokenRepository {
    override fun save(
        userId: UUID,
        tokenHash: String,
        expiresAt: LocalDateTime,
    ) {
        jpa.save(RefreshTokenEntity(UUID.randomUUID(), userId, tokenHash, expiresAt))
    }

    override fun findByHash(tokenHash: String): StoredRefreshToken? =
        jpa.findByTokenHash(tokenHash)?.let {
            StoredRefreshToken(it.userId, it.expiresAt)
        }

    override fun deleteByHash(tokenHash: String) = jpa.deleteByTokenHash(tokenHash)

    override fun deleteAllByUserId(userId: UUID) = jpa.deleteAllByUserId(userId)
}
