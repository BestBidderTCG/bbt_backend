package com.bestbidder.usecase.port

import com.bestbidder.domain.auction.Auction
import java.util.UUID

interface FavoriteAuctionRepository {
    fun addFavorite(
        userId: UUID,
        auctionId: UUID,
    )

    fun removeFavorite(
        userId: UUID,
        auctionId: UUID,
    )

    fun isFavorite(
        userId: UUID,
        auctionId: UUID,
    ): Boolean

    fun getFavoritesByUserId(userId: UUID): List<Auction>
}
