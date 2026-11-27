package com.bestbidder.adapter.web.auction.dto

import com.bestbidder.domain.auction.AuctionStatus
import com.bestbidder.domain.auction.FavoriteAuctionItem
import com.bestbidder.domain.auction.NotificationMode
import com.bestbidder.domain.card.CardState
import java.util.UUID

data class FavoriteAuctionResponse(
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
    val notificationMode: NotificationMode,
) {
    companion object {
        fun fromDomain(item: FavoriteAuctionItem) =
            FavoriteAuctionResponse(
                id = item.auction.id,
                cardName = item.auction.cardName,
                cardImage = item.auction.cardImage,
                currentBid = item.auction.currentBid,
                remainingDays = item.auction.remainingDays,
                cardState = item.auction.cardState,
                cardCategory = item.auction.cardCategory,
                cardCollection = item.auction.cardCollection,
                cardNumber = item.auction.cardNumber,
                status = item.auction.status,
                notificationMode = item.notificationMode,
            )
    }
}
