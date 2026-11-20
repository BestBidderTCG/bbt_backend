package com.bestbidder.adapter.web.bid.dto

import com.bestbidder.domain.auction.AuctionStatus
import com.bestbidder.domain.bid.BidderHistory
import java.util.UUID

data class BidderHistoryResponse(
    val auctionId: UUID,
    val cardName: String,
    val cardImage: String,
    val amount: Double,
    val status: AuctionStatus,
    val iAmWinning: Boolean?,
) {
    companion object {
        fun fromDomain(bidderHistory: BidderHistory): BidderHistoryResponse =
            BidderHistoryResponse(
                auctionId = bidderHistory.auctionId,
                cardName = bidderHistory.cardName,
                cardImage = bidderHistory.cardImage,
                amount = bidderHistory.amount,
                status = bidderHistory.status,
                iAmWinning = bidderHistory.iAmWinning,
            )
    }
}
