package com.bestbidder.domain.bid

import com.bestbidder.domain.auction.AuctionStatus
import java.util.UUID

data class BidderHistory(
    val auctionId: UUID,
    val cardName: String,
    val cardImage: String,
    val amount: Double,
    val status: AuctionStatus,
    val iAmWinning: Boolean?,
)
