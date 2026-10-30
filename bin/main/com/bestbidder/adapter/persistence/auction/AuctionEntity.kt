package com.bestbidder.adapter.persistence.auction

import com.bestbidder.domain.auction.Auction
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "auction")
class AuctionEntity(
    @Id
    val id: UUID = UUID.randomUUID(),
    @Column(name = "creator_id", nullable = false)
    val creatorId: UUID,
    @Column(name = "creator_username", nullable = false)
    val creatorUsername: String,
    @Column(nullable = false)
    val card: String,
    @Column(nullable = false)
    val collection: String,
    @Column(name = "min_prize", nullable = false)
    val minPrize: Double,
    @Column(name = "started_at", nullable = false)
    val startedAt: Instant,
    @Column(name = "duration_in_days", nullable = false)
    val durationInDays: Int,
    @Column(name = "ends_at", nullable = false)
    val endsAt: Instant,
    @Column(name = "is_cancelled", nullable = false)
    val isCancelled: Boolean = false,
) {
    fun toDomain(): Auction {
        return Auction(
            id = this.id,
            creator_id = this.creatorId,
            creator_username = this.creatorUsername,
            card = this.card,
            collection = this.collection,
            min_prize = this.minPrize,
            startedAt = this.startedAt,
            durationInDays = this.durationInDays,
            isCancelled = this.isCancelled,
        )
    }

    companion object {
        fun fromDomain(domain: Auction): AuctionEntity {
            return AuctionEntity(
                id = domain.id,
                creatorId = domain.creator_id,
                creatorUsername = domain.creator_username,
                card = domain.card,
                collection = domain.collection,
                minPrize = domain.min_prize,
                startedAt = domain.startedAt,
                durationInDays = domain.durationInDays,
                endsAt = domain.endsAt,
                isCancelled = domain.isCancelled,
            )
        }
    }
}
