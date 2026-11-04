package com.bestbidder.usecase.bid

import com.bestbidder.domain.bid.Bid
import com.bestbidder.usecase.port.BidRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class GetBidsByAuctionUseCase(
    private val bidRepository: BidRepository,
) {
    @Transactional(readOnly = true)
    fun execute(auctionId: UUID): List<Bid> {
        return bidRepository.findByAuctionIdOrderByAmountDesc(auctionId)
    }
}
