package com.bestbidder.adapter.persistence.auction

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID

interface JpaAuctionViewRepository : JpaRepository<AuctionViewEntity, AuctionViewId> {
    @Modifying
    @Query(
        value = """
            UPDATE auction_view
            SET last_counted_at = :now
            WHERE user_id = :userId
              AND auction_id = :auctionId
              AND last_counted_at <= :threshold
        """,
        nativeQuery = true,
    )
    fun refreshIfWindowElapsed(
        @Param("userId") userId: UUID,
        @Param("auctionId") auctionId: UUID,
        @Param("now") now: Instant,
        @Param("threshold") threshold: Instant,
    ): Int

    @Modifying
    @Query(
        value = """
            INSERT INTO auction_view (user_id, auction_id, last_counted_at)
            VALUES (:userId, :auctionId, :now)
            ON CONFLICT (user_id, auction_id) DO NOTHING
        """,
        nativeQuery = true,
    )
    fun insertIfAbsent(
        @Param("userId") userId: UUID,
        @Param("auctionId") auctionId: UUID,
        @Param("now") now: Instant,
    ): Int
}
