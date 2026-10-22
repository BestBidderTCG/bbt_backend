package com.bestbidder.usecase.port

import com.bestbidder.domain.auction.Auction

interface AuctionRepository {
    fun findById(id: String): Auction?

    fun findAll(): List<Auction>

    fun findByCreator(creator: String): List<Auction>

    fun findByCollection(collection: String): List<Auction>

    fun findByCard(card: String): List<Auction>

    fun existsByCreatorAndCard(
        creator: String,
        card: String,
    ): Boolean

    fun save(auction: Auction): Auction
}
