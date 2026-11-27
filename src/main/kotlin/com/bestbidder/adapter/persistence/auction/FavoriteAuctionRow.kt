package com.bestbidder.adapter.persistence.auction

import com.bestbidder.domain.auction.AuctionListItem
import com.bestbidder.domain.auction.AuctionStatus
import com.bestbidder.domain.auction.FavoriteAuctionItem
import com.bestbidder.domain.auction.NotificationMode
import com.bestbidder.domain.auction.remainingDaysUntil
import com.bestbidder.domain.card.CardState
import java.time.Instant
import java.util.UUID

/**
 * Proyección de una subasta favorita junto con el modo de notificación elegido por el usuario.
 */
@Suppress("LongParameterList")
data class FavoriteAuctionRow(
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
    val notificationMode: NotificationMode,
) {
    fun toFavoriteItem(now: Instant): FavoriteAuctionItem {
        val status =
            when {
                !endsAt.isAfter(now) -> AuctionStatus.FINISHED
                startedAt.isAfter(now) -> AuctionStatus.UPCOMING
                else -> AuctionStatus.IN_PROGRESS
            }

        return FavoriteAuctionItem(
            auction =
                AuctionListItem(
                    id = id,
                    cardName = cardName,
                    cardImage = cardImage,
                    currentBid = currentBid,
                    remainingDays = remainingDaysUntil(endsAt, maxOf(now, startedAt)),
                    cardState = cardState,
                    cardCategory = cardCategory,
                    cardCollection = cardCollection,
                    cardNumber = cardNumber,
                    status = status,
                ),
            notificationMode = notificationMode,
        )
    }
}
