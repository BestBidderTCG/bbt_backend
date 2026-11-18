package com.bestbidder.adapter.persistence.auction

import com.bestbidder.adapter.persistence.card.JpaCardRepository
import com.bestbidder.adapter.persistence.user.JpaUserRepository
import com.bestbidder.domain.auction.Auction
import com.bestbidder.domain.auction.AuctionSummary
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

    override fun getByIdForUpdate(id: UUID): Auction? {
        return jpaAuctionRepository.findByIdForUpdate(id)?.toDomain()
    }

    override fun getAllSummaries(): List<AuctionSummary> {
        val now = Instant.now()
        return jpaAuctionRepository.findAllSummaries(now).map { it.toSummary(now) }
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

    override fun hasActiveAuctionsAsCreator(userId: UUID): Boolean {
        return jpaAuctionRepository.existsActiveOrUpcomingByCreator(userId)
    }

    override fun save(auction: Auction): Auction {
        val creatorRef = jpaUserRepository.getReferenceById(auction.creatorId)
        val cardRef = jpaCardRepository.getReferenceById(auction.card.id)

        val entity = AuctionEntity.fromDomain(auction, creatorRef, cardRef)
        return jpaAuctionRepository.save(entity).toDomain()
    }
}
