package com.bestbidder.adapter.persistence.auction

import com.bestbidder.usecase.auction.AuctionSearchCriteria
import jakarta.persistence.LockModeType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID

interface JpaAuctionRepository : JpaRepository<AuctionEntity, UUID> {
    @Query(
        """
        SELECT a FROM AuctionEntity a
        WHERE (:#{#criteria.query} IS NULL OR LOWER(a.description) LIKE LOWER(CONCAT('%', :#{#criteria.query}, '%')) 
               OR LOWER(a.card.name) LIKE LOWER(CONCAT('%', :#{#criteria.query}, '%')))
          AND (:#{#criteria.onlyActive} IS NULL 
               OR (:#{#criteria.onlyActive} = true AND a.isCancelled = false AND a.endsAt > CURRENT_TIMESTAMP)
               OR (:#{#criteria.onlyActive} = false AND (a.isCancelled = true OR a.endsAt <= CURRENT_TIMESTAMP)))
          AND (:#{#criteria.creator} IS NULL OR a.creator.username = :#{#criteria.creator})
          AND (:#{#criteria.cardId} IS NULL OR a.card.id = :#{#criteria.cardId})
          AND (:#{#criteria.minPrice} IS NULL OR a.minPrize >= :#{#criteria.minPrice})
          AND (:#{#criteria.maxPrice} IS NULL OR a.minPrize <= :#{#criteria.maxPrice})
        """,
    )
    fun search(
        @Param("criteria") criteria: AuctionSearchCriteria,
        pageable: Pageable,
    ): Page<AuctionEntity>

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
