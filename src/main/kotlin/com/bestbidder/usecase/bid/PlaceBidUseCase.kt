package com.bestbidder.usecase.bid

import com.bestbidder.domain.auction.Auction
import com.bestbidder.domain.bid.Bid
import com.bestbidder.domain.bid.BidIncrementPolicy
import com.bestbidder.domain.notification.PushMessages
import com.bestbidder.domain.user.exceptions.UserNotFoundException
import com.bestbidder.usecase.port.AuctionNotificationPort
import com.bestbidder.usecase.port.AuctionRepository
import com.bestbidder.usecase.port.BidRepository
import com.bestbidder.usecase.port.PushNotificationPort
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
    private val pushNotificationPort: PushNotificationPort,
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

        require(previousBid?.bidderId != bidderId) {
            "No puedes pujar si ya tienes la puja más alta actual"
        }

        val minimumAllowedAmount =
            BidIncrementPolicy.calculateMinimumNextBid(
                minPrize = auction.minPrize,
                currentHighestBid = previousBid?.amount,
            )

        require(amount >= minimumAllowedAmount) {
            "El importe mínimo para pujar debe ser de $minimumAllowedAmount €"
        }

        val savedBid =
            bidRepository.save(
                Bid(
                    auctionId = auctionId,
                    bidderId = bidderId,
                    bidderUsername = bidderUsername,
                    amount = amount,
                ),
            )

        notifyBidPlaced(auction, savedBid, previousBid)

        return savedBid
    }

    private fun notifyBidPlaced(
        auction: Auction,
        savedBid: Bid,
        previousBid: Bid?,
    ) {
        notificationPort.notifyNewBidPlaced(
            auctionId = auction.id,
            newAmount = savedBid.amount,
            bidderUsername = savedBid.bidderUsername,
        )

        pushNotificationPort.send(
            listOf(auction.creatorId),
            PushMessages.ownAuctionNewBid(
                auctionId = auction.id,
                cardName = auction.card.name,
                bidderUsername = savedBid.bidderUsername,
                amount = savedBid.amount,
            ),
        )

        previousBid?.let { prev ->
            pushNotificationPort.send(
                listOf(prev.bidderId),
                PushMessages.outbid(
                    auctionId = auction.id,
                    cardName = auction.card.name,
                    newAmount = savedBid.amount,
                ),
            )
        }
    }
}
