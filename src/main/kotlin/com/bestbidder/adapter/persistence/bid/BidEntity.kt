package com.bestbidder.adapter.persistence.bid

import com.bestbidder.adapter.persistence.auction.AuctionEntity
import com.bestbidder.adapter.persistence.user.UserEntity
import com.bestbidder.domain.bid.Bid
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "bid")
class BidEntity(
    @Id
    val id: UUID = UUID.randomUUID(),
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id", nullable = false)
    val auction: AuctionEntity,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bidder_id", nullable = false)
    val bidder: UserEntity,
    @Column(nullable = false)
    val amount: Double,
    @Column(nullable = false)
    val timestamp: Instant = Instant.now(),
) {
    fun toDomain(): Bid {
        return Bid(
            id = id,
            auctionId = auction.id,
            bidderId = bidder.id,
            bidderUsername = bidder.username,
            amount = amount,
            timestamp = timestamp,
        )
    }

    companion object {
        fun fromDomain(
            bid: Bid,
            auctionEntity: AuctionEntity,
            bidderEntity: UserEntity,
        ): BidEntity {
            return BidEntity(
                id = bid.id,
                auction = auctionEntity,
                bidder = bidderEntity,
                amount = bid.amount,
                timestamp = bid.timestamp,
            )
        }
    }
}
