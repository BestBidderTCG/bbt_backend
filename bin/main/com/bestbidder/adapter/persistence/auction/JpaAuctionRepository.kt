package com.bestbidder.adapter.persistence.auction

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface JpaAuctionRepository : JpaRepository<AuctionEntity, UUID> {
    fun findByCreator(creator: String): List<AuctionEntity>

    fun findByCollection(collection: String): List<AuctionEntity>

    fun findByCard(card: String): List<AuctionEntity>

    fun existsByCreatorAndCard(creator: String, card: String): Boolean // <-- Añadir este método
}
