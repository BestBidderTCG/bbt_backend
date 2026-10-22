package com.bestbidder.usecase.port

import com.bestbidder.domain.auction.Auction

interface AuctionRepository {
    fun findById(id: String): Auction?

    fun findByCreator(creator: String): List<Auction>

    fun findByCollection(cart: String): List<Auction>

    fun findByCart(cart: String): List<Auction>

    fun save(auction: Auction): Auction
}
