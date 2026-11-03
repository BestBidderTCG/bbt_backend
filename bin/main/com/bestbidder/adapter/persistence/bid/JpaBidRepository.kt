package com.bestbidder.adapter.persistence.bid

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface JpaBidRepository : JpaRepository<BidEntity, UUID> {
    fun findFirstByAuctionIdOrderByAmountDesc(auctionId: UUID): BidEntity?

    fun findByAuctionIdOrderByAmountDesc(auctionId: UUID): List<BidEntity>
}
