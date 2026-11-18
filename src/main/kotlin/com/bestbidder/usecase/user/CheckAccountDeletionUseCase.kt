package com.bestbidder.usecase.user

import com.bestbidder.usecase.port.AuctionRepository
import com.bestbidder.usecase.port.BidRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class CheckAccountDeletionUseCase(
    private val auctionRepository: AuctionRepository,
    private val bidRepository: BidRepository,
) {
    fun execute(userId: UUID): List<String> {
        val blockers = mutableListOf<String>()

        if (auctionRepository.hasActiveAuctionsAsCreator(userId)) {
            blockers.add("tienes subastas activas o pendientes de comenzar")
        }
        if (bidRepository.isHighestBidderInActiveAuction(userId)) {
            blockers.add("eres el pujador más alto en una subasta en curso")
        }

        return blockers
    }
}
