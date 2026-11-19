package com.bestbidder.domain.auction

import java.util.UUID

data class AuctionSummary(
    val id: UUID,
    val cardName: String,
    val cardImage: String,
    val status: AuctionStatus,
    val minPrize: Double,
    val currentBid: Double,
    val startedAt: Long,
    val endsAt: Long,
    val winnerUsername: String?,
)
