package com.bestbidder.adapter.web.auction.dto

import com.bestbidder.domain.auction.AuctionListItem
import com.bestbidder.domain.auction.AuctionStatus
import com.bestbidder.domain.card.CardState
import java.util.UUID

data class AuctionListItemResponse(
    val id: UUID,
    val cardName: String,
    val cardImage: String,
    val currentBid: Double,
    val remainingDays: Int,
    val cardState: CardState,
    val cardCategory: String,
    val cardCollection: String?,
    val cardNumber: Int?,
    val status: AuctionStatus,
) {
    companion object {
        fun fromDomain(summary: AuctionListItem) =
            AuctionListItemResponse(
                id = summary.id,
                cardName = summary.cardName,
                cardImage = summary.cardImage,
                currentBid = summary.currentBid,
                remainingDays = summary.remainingDays,
                cardState = summary.cardState,
                cardCategory = summary.cardCategory,
                cardCollection = summary.cardCollection,
                cardNumber = summary.cardNumber,
                status = summary.status,
            )
    }
}
