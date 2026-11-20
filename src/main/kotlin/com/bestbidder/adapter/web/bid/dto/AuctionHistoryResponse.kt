package com.bestbidder.adapter.web.bid.dto

import com.bestbidder.domain.bid.AuctionHistory
import java.util.UUID

data class AuctionHistoryResponse(
    val bidderId: UUID,
    val bidderName: String,
    val bidderAvatar: Int,
    val amount: Double,
) {
    companion object {
        fun fromDomain(auctionHistory: AuctionHistory): AuctionHistoryResponse =
            AuctionHistoryResponse(
                bidderId = auctionHistory.bidderId,
                bidderName = auctionHistory.bidderName,
                bidderAvatar = auctionHistory.bidderAvatar,
                amount = auctionHistory.amount,
            )
    }
}
