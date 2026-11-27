package com.bestbidder.usecase.port

import com.bestbidder.domain.auction.FavoriteAuctionItem
import com.bestbidder.domain.auction.NotificationMode
import java.util.UUID

interface FavoriteAuctionRepository {
    fun addFavorite(
        userId: UUID,
        auctionId: UUID,
        notificationMode: NotificationMode,
    )

    fun removeFavorite(
        userId: UUID,
        auctionId: UUID,
    )

    fun isFavorite(
        userId: UUID,
        auctionId: UUID,
    ): Boolean

    fun changeNotificationMode(
        userId: UUID,
        auctionId: UUID,
        notificationMode: NotificationMode,
    )

    fun getFavoritesByUserId(userId: UUID): List<FavoriteAuctionItem>

    fun findUserIdsByAuctionId(auctionId: UUID): List<UUID>
}
