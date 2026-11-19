package com.bestbidder.usecase.user

import com.bestbidder.usecase.port.AuctionRepository
import com.bestbidder.usecase.port.BidRepository
import org.springframework.stereotype.Service
import java.util.UUID

data class AccountDeletionCheckResult(
    val hasActiveAuction: Boolean,
    val isWinningAuction: Boolean,
)

@Service
class CheckAccountDeletionUseCase(
    private val auctionRepository: AuctionRepository,
    private val bidRepository: BidRepository,
) {
    fun execute(userId: UUID): AccountDeletionCheckResult {
        return AccountDeletionCheckResult(
            hasActiveAuction = auctionRepository.hasActiveAuctionsAsCreator(userId),
            isWinningAuction = bidRepository.isHighestBidderInActiveAuction(userId),
        )
    }
}
