package com.bestbidder.adapter.persistence.bid

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface JpaBidRepository : JpaRepository<BidEntity, UUID> {
    fun findFirstByAuctionIdOrderByAmountDesc(auctionId: UUID): BidEntity?

    fun findByAuctionIdOrderByAmountDesc(auctionId: UUID): List<BidEntity>

    @Query(
        """
    SELECT COUNT(b) > 0 FROM BidEntity b
    WHERE b.bidder.id = :bidderId
      AND b.auction.isCancelled = false
      AND b.auction.startedAt <= CURRENT_TIMESTAMP
      AND b.auction.endsAt > CURRENT_TIMESTAMP
      AND b.amount = (
          SELECT MAX(b2.amount) FROM BidEntity b2 WHERE b2.auction.id = b.auction.id
      )
    """,
    )
    fun existsHighestBidInActiveAuction(
        @Param("bidderId") bidderId: UUID,
    ): Boolean

    @Query(
        """
    SELECT new com.bestbidder.adapter.persistence.bid.BidderHistoryRow(
        a.id,
        c.name,
        c.image,
        MAX(b.amount),
        CASE
            WHEN a.isCancelled = true THEN com.bestbidder.domain.auction.AuctionStatus.CANCELLED
            WHEN a.endsAt <= CURRENT_TIMESTAMP THEN com.bestbidder.domain.auction.AuctionStatus.ENDED
            WHEN a.startedAt <= CURRENT_TIMESTAMP THEN com.bestbidder.domain.auction.AuctionStatus.ACTIVE
            ELSE com.bestbidder.domain.auction.AuctionStatus.UPCOMING
        END,
        CASE
            WHEN a.isCancelled = true THEN null
            ELSE (
                MAX(b.amount) = (
                    SELECT MAX(b2.amount) FROM BidEntity b2 WHERE b2.auction.id = a.id
                )
            )
        END
    )
    FROM BidEntity b
    JOIN b.auction a
    JOIN a.card c
    WHERE b.bidder.id = :bidderId
    GROUP BY a.id, c.name, c.image, a.isCancelled, a.startedAt, a.endsAt
    ORDER BY MAX(b.timestamp) DESC
    """,
    )
    fun findBidHistoryByBidderId(
        @Param("bidderId") bidderId: UUID,
    ): List<BidderHistoryRow>

    @Query(
        """
        SELECT new com.bestbidder.adapter.persistence.bid.AuctionHistoryRow(
            b.bidder.id,
            b.bidder.username,
            b.bidder.avatar,
            b.amount
        )
        FROM BidEntity b
        WHERE b.auction.id = :auctionId
        ORDER BY b.amount DESC, b.timestamp DESC
        """,
    )
    fun findBidHistoryByAuctionId(
        @Param("auctionId") auctionId: UUID,
    ): List<AuctionHistoryRow>
}
