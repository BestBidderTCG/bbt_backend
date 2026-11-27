package com.bestbidder.adapter.persistence.auction

import com.bestbidder.domain.auction.NotificationMode
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
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
        SELECT new com.bestbidder.adapter.persistence.auction.FavoriteAuctionRow(
            a.id, c.name, c.image, COALESCE(MAX(b.amount), a.minPrize),
            a.startedAt, a.endsAt, c.state, c.category, c.collection, c.num,
            f.notificationMode
        )
        FROM FavoriteAuctionEntity f
        JOIN f.auction a
        JOIN a.card c
        LEFT JOIN BidEntity b ON b.auction = a
        WHERE f.user.id = :userId
        GROUP BY a.id, c.name, c.image, a.minPrize, a.startedAt, a.endsAt,
                 c.state, c.category, c.collection, c.num, f.notificationMode, f.createdAt
        ORDER BY f.createdAt DESC
        """,
    )
    fun findFavoriteRowsByUserId(
        @Param("userId") userId: UUID,
    ): List<FavoriteAuctionRow>

    @Query(
        """
        SELECT f.user.id FROM FavoriteAuctionEntity f
        WHERE f.auction.id = :auctionId AND f.user.deletedAt IS NULL
        """,
    )
    fun findUserIdsByAuctionId(
        @Param("auctionId") auctionId: UUID,
    ): List<UUID>

    @Modifying(clearAutomatically = true)
    @Query(
        """
        UPDATE FavoriteAuctionEntity f
        SET f.notificationMode = :mode
        WHERE f.user.id = :userId AND f.auction.id = :auctionId
        """,
    )
    fun updateNotificationMode(
        @Param("userId") userId: UUID,
        @Param("auctionId") auctionId: UUID,
        @Param("mode") mode: NotificationMode,
    ): Int
}
