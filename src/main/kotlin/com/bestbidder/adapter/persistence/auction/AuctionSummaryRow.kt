package com.bestbidder.adapter.persistence.auction

import com.bestbidder.domain.auction.AuctionStatus
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
    val startedAt: Instant,
    val endsAt: Instant,
    val cardState: CardState,
    val cardCategory: String,
    val cardCollection: String?,
    val cardNumber: Int?,
) {
    fun toSummary(now: Instant): AuctionSummary =
        AuctionSummary(
            id = id,
            cardName = cardName,
            cardImage = cardImage,
            currentBid = currentBid,
            remainingDays = remainingDaysUntil(endsAt, maxOf(now, startedAt)),
            cardState = cardState,
            cardCategory = cardCategory,
            cardCollection = cardCollection,
            cardNumber = cardNumber,
            status = if (startedAt.isAfter(now)) AuctionStatus.UPCOMING else AuctionStatus.IN_PROGRESS,
        )
}
