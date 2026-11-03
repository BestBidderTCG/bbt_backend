package com.bestbidder.adapter.persistence.bid

import com.bestbidder.domain.bid.Bid
import com.bestbidder.usecase.port.BidRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class BidRepositoryAdapter(
    private val jpaBidRepository: JpaBidRepository,
) : BidRepository {
    override fun save(bid: Bid): Bid {
        val entity = BidEntity.fromDomain(bid)
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
