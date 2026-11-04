package com.bestbidder.adapter.persistence.bid

import com.bestbidder.domain.bid.Bid
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "bids")
class BidEntity(
    @Id
    val id: UUID = UUID.randomUUID(),
    @Column(name = "auction_id", nullable = false)
    val auctionId: UUID,
    @Column(name = "bidder_id", nullable = false)
    val bidderId: UUID,
    @Column(name = "bidder_username", nullable = false)
    val bidderUsername: String,
    @Column(nullable = false)
    val amount: Double,
    @Column(nullable = false)
    val timestamp: Instant = Instant.now(),
) {
    fun toDomain(): Bid {
        return Bid(
            id = id,
            auctionId = auctionId,
            bidderId = bidderId,
            bidderUsername = bidderUsername,
            amount = amount,
            timestamp = timestamp,
        )
    }

    companion object {
        fun fromDomain(bid: Bid): BidEntity {
            return BidEntity(
                id = bid.id,
                auctionId = bid.auctionId,
                bidderId = bid.bidderId,
                bidderUsername = bid.bidderUsername,
                amount = bid.amount,
                timestamp = bid.timestamp,
            )
        }
    }
}
