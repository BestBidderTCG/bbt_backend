package com.bestbidder.adapter.web.auction.dto

import com.bestbidder.domain.auction.AuctionStatus
import com.bestbidder.domain.auction.AuctionSummary
import java.util.UUID

data class AuctionSummaryResponse(
    val id: UUID,
    val cardName: String,
    val cardImage: String,
    val status: AuctionStatus,
    val minPrize: Double,
    val currentBid: Double,
    val startedAt: Long,
    val endsAt: Long,
    val winnerUsername: String?,
) {
    companion object {
        fun fromDomain(summary: AuctionSummary) =
            AuctionSummaryResponse(
                id = summary.id,
                cardName = summary.cardName,
                cardImage = summary.cardImage,
                status = summary.status,
                minPrize = summary.minPrize,
                currentBid = summary.currentBid,
                startedAt = summary.startedAt,
                endsAt = summary.endsAt,
                winnerUsername = summary.winnerUsername,
            )
    }
}
