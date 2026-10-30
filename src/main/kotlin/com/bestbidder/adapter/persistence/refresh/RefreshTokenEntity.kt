package com.bestbidder.adapter.persistence.refresh

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "refresh_tokens")
class RefreshTokenEntity(
    @Id
    val id: UUID,
    @Column(name = "user_id", nullable = false)
    val userId: UUID,
    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    val tokenHash: String,
    @Column(name = "expires_at", nullable = false)
    val expiresAt: LocalDateTime,
)
