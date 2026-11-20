package com.bestbidder.adapter.persistence.auction

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.io.Serializable
import java.time.Instant
import java.util.UUID

@Embeddable
data class AuctionViewId(
    @Column(name = "user_id")
    val userId: UUID,
    @Column(name = "auction_id")
    val auctionId: UUID,
) : Serializable {
    companion object {
        private const val serialVersionUID: Long = 1L
    }
}

@Entity
@Table(name = "auction_view")
class AuctionViewEntity(
    @EmbeddedId
    val id: AuctionViewId,
    @Column(name = "last_counted_at", nullable = false)
    val lastCountedAt: Instant,
)
