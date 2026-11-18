package com.bestbidder.adapter.persistence.auction

import com.bestbidder.domain.auction.AuctionSummary
import com.bestbidder.domain.auction.remainingDaysUntil
import com.bestbidder.domain.card.CardState
import java.time.Instant
import java.util.UUID

data class AuctionSummaryRow(
    val id: UUID,
    val cardName: String,
    val cardImage: String,
    val currentBid: Double,
    val endsAt: Instant,
    val cardState: CardState,
    val cardCategory: String,
    val cardCollection: String?,
) {
    fun toSummary(now: Instant): AuctionSummary =
        AuctionSummary(
            id = id,
            cardName = cardName,
            cardImage = cardImage,
            currentBid = currentBid,
            remainingDays = remainingDaysUntil(endsAt, now),
            cardState = cardState,
            cardCategory = cardCategory,
            cardCollection = cardCollection,
        )
}
