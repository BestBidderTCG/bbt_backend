package com.bestbidder.adapter.persistence.auction

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID

interface JpaAuctionRepository : JpaRepository<AuctionEntity, UUID> {
    fun findByCreatorUsername(creatorUsername: String): List<AuctionEntity>

    fun findByCardId(cardId: UUID): List<AuctionEntity>

    @Query(
        """
        SELECT a FROM AuctionEntity a 
        WHERE a.isCancelled = false 
          AND a.endsAt > CURRENT_TIMESTAMP 
        ORDER BY a.endsAt ASC
    """,
    )
    fun findActive(): List<AuctionEntity>

    @Query(
        """
        SELECT a FROM AuctionEntity a 
        WHERE a.isCancelled = false 
          AND a.startedAt <= CURRENT_TIMESTAMP 
          AND a.endsAt > CURRENT_TIMESTAMP 
        ORDER BY a.endsAt ASC
    """,
    )
    fun findInProgress(): List<AuctionEntity>

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
}
