package com.bestbidder.adapter.persistence.auction

import com.bestbidder.adapter.persistence.card.JpaCardRepository
import com.bestbidder.adapter.persistence.user.JpaUserRepository
import com.bestbidder.domain.auction.Auction
import com.bestbidder.domain.auction.AuctionDetail
import com.bestbidder.domain.auction.AuctionListItem
import com.bestbidder.domain.auction.AuctionSummary
import com.bestbidder.usecase.auction.AuctionSort
import com.bestbidder.usecase.port.AuctionRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

@Repository
@Suppress("TooManyFunctions")
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

    override fun getDetailById(
        id: UUID,
        viewerId: UUID?,
    ): AuctionDetail? {
        val row = jpaAuctionRepository.findDetailById(id, viewerId ?: NO_VIEWER) ?: return null
        return row.toDetail(Instant.now(), viewerId)
    }

    private companion object {
        val NO_VIEWER: UUID = UUID(0L, 0L)
    }

    override fun getByIdForUpdate(id: UUID): Auction? {
        return jpaAuctionRepository.findByIdForUpdate(id)?.toDomain()
    }

    override fun getAllSummaries(sort: AuctionSort): List<AuctionListItem> {
        val now = Instant.now()
        val rows =
            when (sort) {
                AuctionSort.ENDING_SOON -> jpaAuctionRepository.findSummariesEndingSoon(now)
                AuctionSort.ENDING_LATE -> jpaAuctionRepository.findSummariesEndingLate(now)
                AuctionSort.RECENT_FIRST -> jpaAuctionRepository.findSummariesRecentFirst(now)
                AuctionSort.OLDEST_FIRST -> jpaAuctionRepository.findSummariesOldestFirst(now)
                AuctionSort.PRICE_ASC -> jpaAuctionRepository.findSummariesPriceAsc(now)
                AuctionSort.PRICE_DESC -> jpaAuctionRepository.findSummariesPriceDesc(now)
                AuctionSort.NAME_ASC -> jpaAuctionRepository.findSummariesNameAsc(now)
                AuctionSort.NAME_DESC -> jpaAuctionRepository.findSummariesNameDesc(now)
                AuctionSort.NUMBER_ASC -> jpaAuctionRepository.findSummariesNumberAsc(now)
                AuctionSort.NUMBER_DESC -> jpaAuctionRepository.findSummariesNumberDesc(now)
                AuctionSort.POPULARITY -> jpaAuctionRepository.findSummariesPopulariry(now)
            }
        return rows.map { it.toSummary(now) }
    }

    override fun getUserAuctions(id: UUID): List<AuctionSummary> {
        val now = Instant.now()
        val rows = jpaAuctionRepository.findUserAuction(id)
        return rows.map { it.toSummary(now) }
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
