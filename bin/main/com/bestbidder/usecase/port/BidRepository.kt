package com.bestbidder.usecase.port

import com.bestbidder.domain.bid.Bid
import java.util.UUID

interface BidRepository {
    fun findHighestBidByAuctionId(auctionId: UUID): Bid?

    fun findByAuctionIdOrderByAmountDesc(auctionId: UUID): List<Bid>

    fun save(bid: Bid): Bid
}
