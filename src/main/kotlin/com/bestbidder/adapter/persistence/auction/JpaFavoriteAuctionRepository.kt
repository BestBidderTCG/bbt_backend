package com.bestbidder.adapter.persistence.auction

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface JpaFavoriteAuctionRepository : JpaRepository<FavoriteAuctionEntity, UUID> {
    fun existsByUserIdAndAuctionId(
        userId: UUID,
        auctionId: UUID,
    ): Boolean

    fun deleteByUserIdAndAuctionId(
        userId: UUID,
        auctionId: UUID,
    )

    @Query(
        """
        SELECT f.auction FROM FavoriteAuctionEntity f
        JOIN FETCH f.auction.creator
        JOIN FETCH f.auction.card c
        JOIN FETCH c.owner
        WHERE f.user.id = :userId
        ORDER BY f.createdAt DESC
        """,
    )
    fun findFavoriteAuctionsByUserId(
        @Param("userId") userId: UUID,
    ): List<AuctionEntity>
}
