package com.bestbidder.adapter.persistence.auction

import com.bestbidder.adapter.persistence.card.CardEntity
import com.bestbidder.adapter.persistence.user.UserEntity
import com.bestbidder.domain.auction.Auction
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@Entity
@Table(name = "auction")
class AuctionEntity(
    @Id
    val id: UUID = UUID.randomUUID(),
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    val creator: UserEntity,
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false, unique = true)
    val card: CardEntity,
    @Column(nullable = true)
    val description: String? = null,
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
            creator_id = this.creator.id,
            creator_username = this.creator.username,
            card = this.card.toDomain(),
            description = this.description,
            min_prize = this.minPrize,
            startedAt = this.startedAt,
            durationInDays = this.durationInDays,
            isCancelled = this.isCancelled,
        )
    }

    companion object {
        fun fromDomain(
            domain: Auction,
            creatorEntity: UserEntity,
            cardEntity: CardEntity,
        ): AuctionEntity {
            return AuctionEntity(
                id = domain.id,
                creator = creatorEntity,
                card = cardEntity,
                description = domain.description,
                minPrize = domain.min_prize,
                startedAt = domain.startedAt.truncatedTo(ChronoUnit.MINUTES),
                durationInDays = domain.durationInDays,
                endsAt = domain.endsAt.truncatedTo(ChronoUnit.MINUTES),
                isCancelled = domain.isCancelled,
            )
        }
    }
}
