package com.bestbidder.usecase.bid

import com.bestbidder.domain.bid.Bid
import com.bestbidder.domain.bid.BidIncrementPolicy
import com.bestbidder.domain.user.exceptions.UserNotFoundException
import com.bestbidder.usecase.port.AuctionNotificationPort
import com.bestbidder.usecase.port.AuctionRepository
import com.bestbidder.usecase.port.BidRepository
import com.bestbidder.usecase.port.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class PlaceBidUseCase(
    private val auctionRepository: AuctionRepository,
    private val bidRepository: BidRepository,
    private val notificationPort: AuctionNotificationPort,
    private val userRepository: UserRepository,
) {
    @Transactional
    fun execute(
        auctionId: UUID,
        bidderId: UUID,
        bidderUsername: String,
        amount: Double,
    ): Bid {
        val auction =
            auctionRepository.getByIdForUpdate(auctionId)
                ?: throw IllegalArgumentException("La subasta no existe")

        check(auction.getStatusAt(Instant.now()).name == "IN_PROGRESS") {
            "Solo se puede pujar en subastas en curso (IN_PROGRESS)"
        }

        require(bidderId != auction.creatorId) {
            "El creador de la subasta no puede pujar en ella"
        }

        val bidder = userRepository.findById(bidderId) ?: throw UserNotFoundException(bidderId)
        check(bidder.deletedAt == null && !bidder.isPendingDeletion) {
            "Tu cuenta está pendiente de eliminación. Inicia sesión para reactivarla antes de operar."
        }

        val previousBid = bidRepository.findHighestBidByAuctionId(auctionId)

        // Validar que el usuario no sobrepuje su propia puja más alta
        require(previousBid?.bidderId != bidderId) {
            "No puedes pujar si ya tienes la puja más alta actual"
        }

        val currentHighestBidAmount: Double? = previousBid?.amount

        val minimumAllowedAmount: Double =
            BidIncrementPolicy.calculateMinimumNextBid(
                minPrize = auction.minPrize,
                currentHighestBid = currentHighestBidAmount,
            )

        require(amount >= minimumAllowedAmount) {
            "El importe mínimo para pujar debe ser de $minimumAllowedAmount €"
        }

        val newBid =
            Bid(
                auctionId = auctionId,
                bidderId = bidderId,
                bidderUsername = bidderUsername,
                amount = amount,
            )

        val savedBid = bidRepository.save(newBid)

        notificationPort.notifyNewBidPlaced(
            auctionId = auctionId,
            newAmount = amount,
            bidderUsername = bidderUsername,
        )

        return savedBid
    }
}
