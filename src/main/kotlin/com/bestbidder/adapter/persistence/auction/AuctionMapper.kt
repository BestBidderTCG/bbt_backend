package com.bestbidder.adapter.persistence.auction

import com.bestbidder.domain.auction.Auction
import com.bestbidder.usecase.port.AuctionRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class AuctionMapper(
    private val jpaAuctionRepository: JpaAuctionRepository,
) : AuctionRepository {
    override fun findById(id: String): Auction? {
        return jpaAuctionRepository.findById(UUID.fromString(id))
            .map { it.toDomain() }
            .orElse(null)
    }

    override fun findAll(): List<Auction> {
        return jpaAuctionRepository.findAll().map { it.toDomain() }
    }

    override fun findByCreator(creator: String): List<Auction> {
        return jpaAuctionRepository.findByCreator(creator).map { it.toDomain() }
    }

    override fun findByCollection(collection: String): List<Auction> {
        return jpaAuctionRepository.findByCollection(collection).map { it.toDomain() }
    }

    override fun findByCard(card: String): List<Auction> {
        return jpaAuctionRepository.findByCard(card).map { it.toDomain() }
    }

    override fun existsByCreatorAndCard(
        creator: String,
        card: String,
    ): Boolean {
        return jpaAuctionRepository.existsByCreatorAndCard(creator, card)
    }

    override fun save(auction: Auction): Auction {
        val entity = AuctionEntity.fromDomain(auction)
        return jpaAuctionRepository.save(entity).toDomain()
    }
}
