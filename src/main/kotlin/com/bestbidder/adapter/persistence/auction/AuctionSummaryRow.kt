package com.bestbidder.adapter.persistence.auction

import com.bestbidder.domain.auction.AuctionStatus
import com.bestbidder.domain.auction.AuctionSummary
import java.time.Instant
import java.util.UUID

data class AuctionSummaryRow(
    val id: UUID,
    val cardName: String,
    val cardImage: String,
    val minPrize: Double,
    val currentBid: Double,
    val startedAt: Instant,
    val endsAt: Instant,
    val winnerUsername: String?,
) {
    fun toSummary(now: Instant): AuctionSummary {
        val status =
            when {
                !endsAt.isAfter(now) -> AuctionStatus.FINISHED
                startedAt.isAfter(now) -> AuctionStatus.UPCOMING
                else -> AuctionStatus.IN_PROGRESS
            }

        return AuctionSummary(
            id = id,
            cardName = cardName,
            cardImage = cardImage,
            status = status,
            minPrize = minPrize,
            currentBid = currentBid,
            startedAt = startedAt.toEpochMilli(),
            endsAt = endsAt.toEpochMilli(),
            winnerUsername = if (status == AuctionStatus.FINISHED) winnerUsername else null,
        )
    }
}
