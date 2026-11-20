package com.bestbidder.adapter.persistence.bid

import com.bestbidder.domain.bid.AuctionHistory
import java.util.UUID

data class AuctionHistoryRow(
    val bidderId: UUID,
    val bidderName: String,
    val bidderAvatar: String,
    val amount: Double,
) {
    fun toDomain() =
        AuctionHistory(
            bidderId = bidderId,
            bidderName = bidderName,
            bidderAvatar = bidderAvatar,
            amount = amount,
        )
}
