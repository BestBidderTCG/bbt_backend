package com.bestbidder.adapter.persistence.auction

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID

interface JpaAuctionRepository : JpaRepository<AuctionEntity, UUID> {
    @Query(
        """
        SELECT new com.bestbidder.adapter.persistence.auction.AuctionSummaryRow(
            a.id,
            c.name,
            c.image,
            COALESCE(MAX(b.amount), a.minPrize),
            a.endsAt,
            c.state,
            c.category,
            c.collection
        )
        FROM AuctionEntity a
        JOIN a.card c
        LEFT JOIN BidEntity b ON b.auction = a
        WHERE a.isCancelled = false
          AND a.endsAt > :now
        GROUP BY a.id, c.name, c.image, a.minPrize, a.endsAt, c.state, c.category, c.collection
        ORDER BY a.endsAt ASC
        """,
    )
    fun findAllSummaries(
        @Param("now") now: Instant,
    ): List<AuctionSummaryRow>

    @Query(
        """
        SELECT COUNT(a) > 0 FROM AuctionEntity a 
        WHERE a.creator.username = :creator 
          AND a.card.id = :cardId 
          AND a.isCancelled = false 
          AND a.endsAt > CURRENT_TIMESTAMP
        """,
    )
    fun existsActiveAuction(
        @Param("creator") creator: String,
        @Param("cardId") cardId: UUID,
    ): Boolean

    @Query(
        """
        SELECT a FROM AuctionEntity a 
        WHERE a.isCancelled = false 
          AND a.startedAt = :now 
        """,
    )
    fun findJustStarted(
        @Param("now") now: Instant,
    ): List<AuctionEntity>

    @Query(
        """
        SELECT a FROM AuctionEntity a 
        WHERE a.isCancelled = false 
          AND a.endsAt = :now
        """,
    )
    fun findJustExpired(
        @Param("now") now: Instant,
    ): List<AuctionEntity>

    @Query(
        """
    SELECT COUNT(a) > 0 FROM AuctionEntity a
    WHERE a.creator.id = :userId
      AND a.isCancelled = false
      AND a.endsAt > CURRENT_TIMESTAMP
    """,
    )
    fun existsActiveOrUpcomingByCreator(
        @Param("userId") userId: UUID,
    ): Boolean

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM AuctionEntity a WHERE a.id = :id")
    fun findByIdForUpdate(
        @Param("id") id: UUID,
    ): AuctionEntity?
}
