package com.bestbidder.adapter.persistence.auction

import com.bestbidder.domain.auction.Auction
import com.bestbidder.usecase.port.AuctionRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

@Repository
class AuctionRepositoryAdapter(
    private val jpaAuctionRepository: JpaAuctionRepository,
) : AuctionRepository {
    override fun getById(id: UUID): Auction? {
        return jpaAuctionRepository.findById(id)
            .map { it.toDomain() }
            .orElse(null)
    }

    override fun getAll(): List<Auction> {
        return jpaAuctionRepository.findAll().map { it.toDomain() }
    }

    override fun getByCreator(creator: String): List<Auction> {
        return jpaAuctionRepository.findByCreatorUsername(creator).map { it.toDomain() }
    }

    override fun getByCollection(collection: String): List<Auction> {
        return jpaAuctionRepository.findByCollection(collection).map { it.toDomain() }
    }

    override fun getByCard(card: String): List<Auction> {
        return jpaAuctionRepository.findByCard(card).map { it.toDomain() }
    }

    override fun getActive(): List<Auction> {
        return jpaAuctionRepository.findActive().map { it.toDomain() }
    }

    override fun getInProgress(): List<Auction> {
        return jpaAuctionRepository.findInProgress().map { it.toDomain() }
    }

    override fun existsByCreatorAndCard(
        creator: String,
        card: String,
    ): Boolean {
        return jpaAuctionRepository.existsActiveAuction(creator, card)
    }

    override fun getJustStarted(now: Instant): List<Auction> {
        return jpaAuctionRepository.findJustStarted(now).map { it.toDomain() }
    }

    override fun getJustExpired(now: Instant): List<Auction> {
        return jpaAuctionRepository.findJustExpired(now).map { it.toDomain() }
    }

    override fun save(auction: Auction): Auction {
        val entity = AuctionEntity.fromDomain(auction)
        return jpaAuctionRepository.save(entity).toDomain()
    }
}
