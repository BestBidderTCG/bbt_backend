package com.bestbidder.usecase.port

import java.time.LocalDateTime
import java.util.UUID

interface RefreshTokenRepository {
    fun save(
        userId: UUID,
        tokenHash: String,
        expiresAt: LocalDateTime,
    )

    fun findByHash(tokenHash: String): StoredRefreshToken?

    fun deleteByHash(tokenHash: String)

    fun deleteAllByUserId(userId: UUID)
}

data class StoredRefreshToken(val userId: UUID, val expiresAt: LocalDateTime)
