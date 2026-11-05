package com.bestbidder.adapter.persistence.auction

import com.bestbidder.adapter.persistence.card.JpaCardRepository
import com.bestbidder.adapter.persistence.user.JpaUserRepository
import com.bestbidder.domain.auction.Auction
import com.bestbidder.usecase.port.AuctionRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

@Repository
class AuctionRepositoryAdapter(
    private val jpaAuctionRepository: JpaAuctionRepository,
    private val jpaUserRepository: JpaUserRepository,
    private val jpaCardRepository: JpaCardRepository,
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

    override fun getByCard(cardId: UUID): List<Auction> {
        return jpaAuctionRepository.findByCardId(cardId).map { it.toDomain() }
    }

    override fun getActive(): List<Auction> {
        return jpaAuctionRepository.findActive().map { it.toDomain() }
    }

    override fun getInProgress(): List<Auction> {
        return jpaAuctionRepository.findInProgress().map { it.toDomain() }
    }

    override fun existsByCreatorAndCard(
        creator: String,
        cardId: UUID,
    ): Boolean {
        return jpaAuctionRepository.existsActiveAuction(creator, cardId)
    }

    override fun getJustStarted(now: Instant): List<Auction> {
        return jpaAuctionRepository.findJustStarted(now).map { it.toDomain() }
    }

    override fun getJustExpired(now: Instant): List<Auction> {
        return jpaAuctionRepository.findJustExpired(now).map { it.toDomain() }
    }

    override fun save(auction: Auction): Auction {
        val creatorRef = jpaUserRepository.getReferenceById(auction.creator_id)
        val cardRef = jpaCardRepository.getReferenceById(auction.card.id)

        val entity = AuctionEntity.fromDomain(auction, creatorRef, cardRef)
        return jpaAuctionRepository.save(entity).toDomain()
    }
}
