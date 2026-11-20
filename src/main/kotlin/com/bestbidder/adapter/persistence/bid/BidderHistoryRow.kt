package com.bestbidder.adapter.persistence.bid

import com.bestbidder.domain.auction.AuctionStatus
import com.bestbidder.domain.bid.BidderHistory
import java.time.Instant
import java.util.UUID

@Suppress("LongParameterList")
data class BidderHistoryRow(
    val auctionId: UUID,
    val cardName: String,
    val cardImage: String,
    val amount: Double,
    val isCancelled: Boolean,
    val startedAt: Instant,
    val endsAt: Instant,
    val iAmWinning: Boolean,
) {
    fun toDomain(now: Instant): BidderHistory {
        val status =
            when {
                isCancelled -> AuctionStatus.CANCELLED
                endsAt <= now -> AuctionStatus.FINISHED
                startedAt <= now -> AuctionStatus.IN_PROGRESS
                else -> AuctionStatus.UPCOMING
            }

        return BidderHistory(
            auctionId = auctionId,
            cardName = cardName,
            cardImage = cardImage,
            amount = amount,
            status = status,
            iAmWinning = if (isCancelled) null else iAmWinning,
        )
    }
}
