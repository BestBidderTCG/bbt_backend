package com.bestbidder.adapter.persistence.bid

import com.bestbidder.adapter.persistence.auction.JpaAuctionRepository
import com.bestbidder.adapter.persistence.user.JpaUserRepository
import com.bestbidder.domain.bid.Bid
import com.bestbidder.usecase.port.BidRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class BidRepositoryAdapter(
    private val jpaBidRepository: JpaBidRepository,
    private val jpaAuctionRepository: JpaAuctionRepository,
    private val jpaUserRepository: JpaUserRepository,
) : BidRepository {
    override fun save(bid: Bid): Bid {
        val auctionRef = jpaAuctionRepository.getReferenceById(bid.auctionId)
        val bidderRef = jpaUserRepository.getReferenceById(bid.bidderId)

        val entity = BidEntity.fromDomain(bid, auctionRef, bidderRef)
        return jpaBidRepository.save(entity).toDomain()
    }

    override fun findHighestBidByAuctionId(auctionId: UUID): Bid? {
        return jpaBidRepository.findFirstByAuctionIdOrderByAmountDesc(auctionId)?.toDomain()
    }

    override fun findByAuctionIdOrderByAmountDesc(auctionId: UUID): List<Bid> {
        return jpaBidRepository.findByAuctionIdOrderByAmountDesc(auctionId)
            .map { it.toDomain() }
    }
}
