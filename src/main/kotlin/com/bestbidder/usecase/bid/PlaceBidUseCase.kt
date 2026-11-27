package com.bestbidder.usecase.bid

import com.bestbidder.domain.auction.Auction
import com.bestbidder.domain.auction.NotificationMode
import com.bestbidder.domain.bid.Bid
import com.bestbidder.domain.bid.BidIncrementPolicy
import com.bestbidder.domain.bid.exceptions.BidOutbidException
import com.bestbidder.domain.notification.Audience
import com.bestbidder.domain.notification.PushMessages
import com.bestbidder.domain.notification.SseEvents
import com.bestbidder.domain.user.User
import com.bestbidder.domain.user.exceptions.UserNotFoundException
import com.bestbidder.domain.wallet.TransactionType
import com.bestbidder.domain.wallet.WalletTransaction
import com.bestbidder.domain.wallet.exceptions.InsufficientBalanceException
import com.bestbidder.usecase.port.AuctionRepository
import com.bestbidder.usecase.port.BidRepository
import com.bestbidder.usecase.port.FavoriteAuctionRepository
import com.bestbidder.usecase.port.PushNotificationPort
import com.bestbidder.usecase.port.RealtimeEventPort
import com.bestbidder.usecase.port.UserRepository
import com.bestbidder.usecase.port.WalletTransactionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
@Suppress("LongParameterList")
class PlaceBidUseCase(
    private val auctionRepository: AuctionRepository,
    private val bidRepository: BidRepository,
    private val realtimeEventPort: RealtimeEventPort,
    private val userRepository: UserRepository,
    private val walletTransactionRepository: WalletTransactionRepository,
    private val pushNotificationPort: PushNotificationPort,
    private val favoriteAuctionRepository: FavoriteAuctionRepository,
) {
    @Transactional
    fun execute(
        auctionId: UUID,
        bidderId: UUID,
        bidderUsername: String,
        amount: Double,
    ): Bid {
        val auction = loadBiddableAuction(auctionId, bidderId)
        val bidder = loadActiveBidder(bidderId)

        // Lectura de la puja más alta ya actualizada tras el lock
        val previousBid = bidRepository.findHighestBidByAuctionId(auctionId)
        validateAmount(auction, previousBid, bidderId, amount)

        holdBidderBalance(bidder, auction, amount)
        val refundedBalance = previousBid?.let { refundPreviousBidder(auction, it) }

        val savedBid =
            bidRepository.save(
                Bid(
                    auctionId = auctionId,
                    bidderId = bidderId,
                    bidderUsername = bidderUsername,
                    amount = amount,
                ),
            )

        notifyBidPlaced(auction, savedBid, previousBid, refundedBalance)

        return savedBid
    }

    /** Bloqueo pesimista: si dos usuarios pujan a la vez, el segundo espera aquí. */
    private fun loadBiddableAuction(
        auctionId: UUID,
        bidderId: UUID,
    ): Auction {
        val auction =
            auctionRepository.getByIdForUpdate(auctionId)
                ?: throw IllegalArgumentException("La subasta no existe")

        check(auction.getStatusAt(Instant.now()).name == "IN_PROGRESS") {
            "Solo se puede pujar en subastas en curso (IN_PROGRESS)"
        }

        require(bidderId != auction.creatorId) {
            "El creador de la subasta no puede pujar en ella"
        }
        return auction
    }

    private fun loadActiveBidder(bidderId: UUID): User {
        val bidder = userRepository.findById(bidderId) ?: throw UserNotFoundException(bidderId)
        check(bidder.deletedAt == null && !bidder.isPendingDeletion) {
            "Tu cuenta está pendiente de eliminación. Inicia sesión para reactivarla antes de operar."
        }
        return bidder
    }

    private fun validateAmount(
        auction: Auction,
        previousBid: Bid?,
        bidderId: UUID,
        amount: Double,
    ) {
        require(previousBid?.bidderId != bidderId) {
            "No puedes pujar si ya tienes la puja más alta actual"
        }

        val minimumAllowedAmount =
            BidIncrementPolicy.calculateMinimumNextBid(
                minPrize = auction.minPrize,
                currentHighestBid = previousBid?.amount,
            )

        // Concurrencia: alguien ha pujado igual o más mientras estabas en la pantalla
        if (previousBid != null && amount <= previousBid.amount) {
            throw BidOutbidException(
                "Ya se ha realizado una puja mayor o igual a la actual (${previousBid.amount} €). " +
                    "El nuevo mínimo para pujar es de $minimumAllowedAmount €",
            )
        }

        require(amount >= minimumAllowedAmount) {
            "El importe mínimo para pujar debe ser de $minimumAllowedAmount €"
        }
    }

    /** Retención de saldo del nuevo pujador (atómico en BD). */
    private fun holdBidderBalance(
        bidder: User,
        auction: Auction,
        amount: Double,
    ) {
        val newBalanceAfterHold =
            userRepository.deductBalance(bidder.id, amount)
                ?: throw InsufficientBalanceException(
                    "Saldo insuficiente para pujar $amount € (saldo disponible: ${bidder.balance} €)",
                )

        walletTransactionRepository.save(
            WalletTransaction(
                userId = bidder.id,
                type = TransactionType.BID_HOLD,
                amount = amount,
                balanceAfter = newBalanceAfterHold,
                referenceId = auction.id,
                description = "Retención por puja en ${auction.card.name}",
            ),
        )
    }

    /** Liberación del saldo retenido al pujador anterior. Devuelve su nuevo saldo. */
    private fun refundPreviousBidder(
        auction: Auction,
        previousBid: Bid,
    ): Double {
        val previousBidderNewBalance = userRepository.addBalance(previousBid.bidderId, previousBid.amount)
        walletTransactionRepository.save(
            WalletTransaction(
                userId = previousBid.bidderId,
                type = TransactionType.BID_REFUND,
                amount = previousBid.amount,
                balanceAfter = previousBidderNewBalance,
                referenceId = auction.id,
                description = "Devolución por puja superada en ${auction.card.name}",
            ),
        )
        return previousBidderNewBalance
    }

    private fun notifyBidPlaced(
        auction: Auction,
        savedBid: Bid,
        previousBid: Bid?,
        refundedBalance: Double?,
    ) {
        realtimeEventPort.publish(
            SseEvents.bidPlaced(auction.id, savedBid.amount, savedBid.bidderUsername),
            Audience.Everyone,
        )
        if (previousBid != null && refundedBalance != null) {
            realtimeEventPort.publish(
                SseEvents.balanceChanged(auction.id, refundedBalance),
                Audience.Users(previousBid.bidderId),
            )
        }

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

        // Favoritos con modo ALL; sin el pujador ni el superado, que ya tienen su propio aviso
        val favoriteUserIds =
            favoriteAuctionRepository.findUserIdsByAuctionId(auction.id, NotificationMode.BIDS)
                .filter { it != savedBid.bidderId && it != previousBid?.bidderId }
        pushNotificationPort.send(
            favoriteUserIds,
            PushMessages.favoriteNewBid(
                auctionId = auction.id,
                cardName = auction.card.name,
                amount = savedBid.amount,
            ),
        )
    }
}
