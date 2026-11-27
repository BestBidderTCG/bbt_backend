package com.bestbidder.usecase.auction

import com.bestbidder.domain.auction.NotificationMode
import com.bestbidder.domain.notification.Audience
import com.bestbidder.domain.notification.PushMessages
import com.bestbidder.domain.notification.SseEvents
import com.bestbidder.usecase.port.AuctionRepository
import com.bestbidder.usecase.port.BidRepository
import com.bestbidder.usecase.port.FavoriteAuctionRepository
import com.bestbidder.usecase.port.PushNotificationPort
import com.bestbidder.usecase.port.RealtimeEventPort
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class CloseExpiredAuctionsUseCase(
    private val auctionRepository: AuctionRepository,
    private val bidRepository: BidRepository,
    private val realtimeEventPort: RealtimeEventPort,
    private val favoriteAuctionRepository: FavoriteAuctionRepository,
    private val pushNotificationPort: PushNotificationPort,
) {
    fun execute(now: Instant) {
        auctionRepository.getJustExpired(now).forEach { auction ->
            val winningBid = bidRepository.findHighestBidByAuctionId(auction.id)
            val cardName = auction.card.name

            realtimeEventPort.publish(SseEvents.auctionEnded(auction.id, winningBid), Audience.Everyone)

            // Push al creador: subasta terminada, con ganador e importe si lo hubo
            pushNotificationPort.send(
                listOf(auction.creatorId),
                PushMessages.ownAuctionEnded(auction.id, cardName, winningBid),
            )

            // Push al ganador
            winningBid?.let {
                pushNotificationPort.send(
                    listOf(it.bidderId),
                    PushMessages.auctionWon(auction.id, cardName, it.amount),
                )
            }

            // Push a quienes la tenían en favoritos (excluyendo al ganador para no duplicar aviso)
            val favoriteUserIds =
                favoriteAuctionRepository.findUserIdsByAuctionId(auction.id, NotificationMode.LIFECYCLE)
                    .filter { it != winningBid?.bidderId }

            pushNotificationPort.send(
                favoriteUserIds,
                PushMessages.favoriteEnded(auction.id, cardName),
            )
        }
    }
}
