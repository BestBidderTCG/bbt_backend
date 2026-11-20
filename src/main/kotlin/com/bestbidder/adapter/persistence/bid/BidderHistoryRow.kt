package com.bestbidder.adapter.persistence.bid

import com.bestbidder.domain.auction.AuctionStatus
import com.bestbidder.domain.bid.BidderHistory
import java.util.UUID

data class BidderHistoryRow(
    val auctionId: UUID,
    val cardName: String,
    val cardImage: String,
    val amount: Double,
    val status: AuctionStatus,
    val iAmWinning: Boolean?,
) {
    fun toDomain() =
        BidderHistory(
            auctionId = auctionId,
            cardName = cardName,
            cardImage = cardImage,
            amount = amount,
            status = status,
            iAmWinning = iAmWinning,
        )
}
