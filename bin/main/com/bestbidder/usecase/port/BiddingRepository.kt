package com.bestbidder.usecase.port

import com.bestbidder.domain.bid.Bid

interface BiddingRepository {
    fun findById(id: String): Bid?

    fun findByCreator(creator: String): List<Bid>

    fun findByAuction(cart: String): List<Bid>

    fun save(bid: Bid): Bid
}
