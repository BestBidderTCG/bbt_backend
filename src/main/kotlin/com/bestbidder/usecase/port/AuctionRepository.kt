package com.bestbidder.usecase.port

import com.bestbidder.domain.auction.Auction

interface AuctionRepository {
    fun findById(id: String): Auction?

    fun findByCreator(creator: String): List<Auction>

    fun findByCollection(collection: String): List<Auction>

    fun findByCard(card: String): List<Auction>

    // Retorna true si ya existe al menos una subasta de este creador para esa carta
    fun existsByCreatorAndCard(
        creator: String,
        card: String,
    ): Boolean

    fun save(auction: Auction): Auction
}
