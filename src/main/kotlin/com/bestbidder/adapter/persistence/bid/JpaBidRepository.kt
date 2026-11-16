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
}
