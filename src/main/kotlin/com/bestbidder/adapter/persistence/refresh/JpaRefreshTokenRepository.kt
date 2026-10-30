package com.bestbidder.adapter.persistence.refresh

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface JpaRefreshTokenRepository : JpaRepository<RefreshTokenEntity, UUID> {
    fun findByTokenHash(tokenHash: String): RefreshTokenEntity?

    fun deleteByTokenHash(tokenHash: String)

    fun deleteAllByUserId(userId: UUID)
}
