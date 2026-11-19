package com.bestbidder.adapter.persistence.auction

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID

private const val SUMMARY_SELECT = """
    SELECT new com.bestbidder.adapter.persistence.auction.AuctionListItemRow(
        a.id, c.name, c.image, COALESCE(MAX(b.amount), a.minPrize),
        a.startedAt, a.endsAt, c.state, c.category, c.collection, c.num
    )
    FROM AuctionEntity a
    JOIN a.card c
    LEFT JOIN BidEntity b ON b.auction = a
    WHERE a.isCancelled = false AND a.endsAt > :now
    GROUP BY a.id, c.name, c.image, a.minPrize, a.startedAt, a.endsAt, c.state, c.category, c.collection, c.num
"""

@Suppress("TooManyFunctions")
interface JpaAuctionRepository : JpaRepository<AuctionEntity, UUID> {
    @Query("$SUMMARY_SELECT ORDER BY a.endsAt ASC")
    fun findSummariesEndingSoon(
        @Param("now") now: Instant,
    ): List<AuctionListItemRow>

    @Query("$SUMMARY_SELECT ORDER BY a.endsAt DESC")
    fun findSummariesEndingLate(
        @Param("now") now: Instant,
    ): List<AuctionListItemRow>

    @Query("$SUMMARY_SELECT ORDER BY a.startedAt DESC")
    fun findSummariesRecentFirst(
        @Param("now") now: Instant,
    ): List<AuctionListItemRow>

    @Query("$SUMMARY_SELECT ORDER BY a.startedAt ASC")
    fun findSummariesOldestFirst(
        @Param("now") now: Instant,
    ): List<AuctionListItemRow>

    @Query("$SUMMARY_SELECT ORDER BY COALESCE(MAX(b.amount), a.minPrize) ASC, a.endsAt ASC")
    fun findSummariesPriceAsc(
        @Param("now") now: Instant,
    ): List<AuctionListItemRow>

    @Query("$SUMMARY_SELECT ORDER BY COALESCE(MAX(b.amount), a.minPrize) DESC, a.endsAt ASC")
    fun findSummariesPriceDesc(
        @Param("now") now: Instant,
    ): List<AuctionListItemRow>

    @Query("$SUMMARY_SELECT ORDER BY LOWER(c.name) ASC, a.endsAt ASC")
    fun findSummariesNameAsc(
        @Param("now") now: Instant,
    ): List<AuctionListItemRow>

    @Query("$SUMMARY_SELECT ORDER BY LOWER(c.name) DESC, a.endsAt ASC")
    fun findSummariesNameDesc(
        @Param("now") now: Instant,
    ): List<AuctionListItemRow>

    @Query("$SUMMARY_SELECT ORDER BY c.num ASC NULLS LAST, a.endsAt ASC")
    fun findSummariesNumberAsc(
        @Param("now") now: Instant,
    ): List<AuctionListItemRow>

    @Query("$SUMMARY_SELECT ORDER BY c.num DESC NULLS LAST, a.endsAt ASC")
    fun findSummariesNumberDesc(
        @Param("now") now: Instant,
    ): List<AuctionListItemRow>

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

    @Query(
        """
    SELECT new com.bestbidder.adapter.persistence.auction.AuctionSummaryRow(
        a.id,
        c.name,
        c.image,
        a.minPrize,
        COALESCE((SELECT MAX(b.amount) FROM BidEntity b WHERE b.auction = a), a.minPrize),
        a.startedAt,
        a.endsAt,
        (SELECT b2.bidder.username FROM BidEntity b2
         WHERE b2.auction = a
           AND b2.amount = (SELECT MAX(b3.amount) FROM BidEntity b3 WHERE b3.auction = a))
        )
        FROM AuctionEntity a
        JOIN a.card c
        WHERE a.creator.id = :userId
        AND a.isCancelled = false
        ORDER BY a.endsAt DESC
        """,
    )
    fun findUserAuction(
        @Param("userId") userId: UUID,
    ): List<AuctionSummaryRow>

    @Query(
        """
    SELECT new com.bestbidder.adapter.persistence.auction.AuctionDetailRow(
        a.id,
        c.name,
        c.image,
        c.collection,
        c.num,
        c.state,
        a.minPrize,
        (SELECT MAX(b.amount) FROM BidEntity b WHERE b.auction = a),
        (SELECT COUNT(b2) FROM BidEntity b2 WHERE b2.auction = a),
        a.startedAt,
        a.endsAt,
        a.description,
        a.isCancelled,
        u.id,
        u.username,
        u.avatarNum,
        CASE WHEN EXISTS (
            SELECT 1 FROM FavoriteAuctionEntity f
            WHERE f.auction = a AND f.user.id = :viewerId
        ) THEN TRUE ELSE FALSE END
    )
    FROM AuctionEntity a
    JOIN a.card c
    JOIN a.creator u
    WHERE a.id = :id
    """,
    )
    fun findDetailById(
        @Param("id") id: UUID,
        @Param("viewerId") viewerId: UUID,
    ): AuctionDetailRow?
}
