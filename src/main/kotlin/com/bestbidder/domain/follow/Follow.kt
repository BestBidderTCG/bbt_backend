package com.bestbidder.domain.follow

import java.time.Instant
import java.util.UUID

/**
 * Relación de seguimiento: [followerId] sigue a [followeeId].
 */
data class Follow(
    val id: UUID = UUID.randomUUID(),
    val followerId: UUID,
    val followeeId: UUID,
    val createdAt: Instant = Instant.now(),
) {
    init {
        require(followerId != followeeId) { "No puedes seguirte a ti mismo" }
    }
}
