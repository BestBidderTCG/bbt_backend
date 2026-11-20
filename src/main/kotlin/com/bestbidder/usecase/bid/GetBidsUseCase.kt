package com.bestbidder.usecase.bid

import com.bestbidder.domain.bid.AuctionHistory
import com.bestbidder.domain.bid.BidderHistory
import com.bestbidder.usecase.port.BidRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class GetBidsUseCase(
    private val bidRepository: BidRepository,
) {
    fun byAuction(auctionId: UUID): List<AuctionHistory> = bidRepository.getBidHistoryByAuction(auctionId)

    fun byBidder(bidderId: UUID): List<BidderHistory> = bidRepository.getBidHistoryByUser(bidderId)
}
