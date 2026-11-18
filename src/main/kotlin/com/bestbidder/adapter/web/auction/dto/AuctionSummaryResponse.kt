package com.bestbidder.adapter.web.auction.dto

import com.bestbidder.domain.auction.AuctionSummary
import com.bestbidder.domain.card.CardState
import java.util.UUID

data class AuctionSummaryResponse(
    val id: UUID,
    val cardName: String,
    val cardImage: String,
    val currentBid: Double,
    val remainingDays: Int,
    val cardState: CardState,
    val cardCategory: String,
    val cardCollection: String?,
) {
    companion object {
        fun fromDomain(summary: AuctionSummary) =
            AuctionSummaryResponse(
                id = summary.id,
                cardName = summary.cardName,
                cardImage = summary.cardImage,
                currentBid = summary.currentBid,
                remainingDays = summary.remainingDays,
                cardState = summary.cardState,
                cardCategory = summary.cardCategory,
                cardCollection = summary.cardCollection,
            )
    }
}
