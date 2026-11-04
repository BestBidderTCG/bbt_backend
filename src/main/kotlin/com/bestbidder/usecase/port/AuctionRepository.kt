package com.bestbidder.usecase.port

import com.bestbidder.domain.auction.Auction
import java.time.Instant
import java.util.UUID

interface AuctionRepository {
    fun getById(id: UUID): Auction?

    fun getAll(): List<Auction>

    fun getByCreator(creator: String): List<Auction>

    fun getByCollection(collection: String): List<Auction>

    fun getByCard(card: String): List<Auction>

    fun getActive(): List<Auction>

    fun getInProgress(): List<Auction>

    fun existsByCreatorAndCard(
        creator: String,
        card: String,
    ): Boolean

    fun getJustStarted(now: Instant): List<Auction>

    fun getJustExpired(now: Instant): List<Auction>

    fun save(auction: Auction): Auction
}
