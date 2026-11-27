package com.bestbidder.usecase.auction

import com.bestbidder.domain.auction.FavoriteAuctionItem
import com.bestbidder.domain.auction.NotificationMode
import com.bestbidder.usecase.port.AuctionRepository
import com.bestbidder.usecase.port.FavoriteAuctionRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ManageFavoriteAuctionUseCase(
    private val favoriteAuctionRepository: FavoriteAuctionRepository,
    private val auctionRepository: AuctionRepository,
) {
    fun addFavorite(
        userId: UUID,
        auctionId: UUID,
        notificationMode: NotificationMode,
    ) {
        val auction =
            auctionRepository.getById(auctionId)
                ?: throw IllegalArgumentException("No existe la subasta con ID $auctionId")

        require(auction.creatorId != userId) {
            "No puedes añadir tu propia subasta a favoritos"
        }

        favoriteAuctionRepository.addFavorite(userId, auctionId, notificationMode)
    }

    fun removeFavorite(
        userId: UUID,
        auctionId: UUID,
    ) {
        favoriteAuctionRepository.removeFavorite(userId, auctionId)
    }

    fun changeNotificationMode(
        userId: UUID,
        auctionId: UUID,
        notificationMode: NotificationMode,
    ) {
        favoriteAuctionRepository.changeNotificationMode(userId, auctionId, notificationMode)
    }

    fun getUserFavorites(userId: UUID): List<FavoriteAuctionItem> =
        favoriteAuctionRepository.getFavoritesByUserId(
            userId,
        )
}
