package com.bestbidder.adapter.web.auction.dto

import com.bestbidder.domain.auction.AuctionStatus
import com.bestbidder.domain.auction.UserAuctionSummary
import java.util.UUID

data class UserAuctionSummaryResponse(
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
        fun fromDomain(summary: UserAuctionSummary) =
            UserAuctionSummaryResponse(
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
