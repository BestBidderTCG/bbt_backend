package com.bestbidder.usecase.auction

import com.bestbidder.domain.notification.PushMessages
import com.bestbidder.usecase.port.AuctionNotificationPort
import com.bestbidder.usecase.port.AuctionRepository
import com.bestbidder.usecase.port.FavoriteAuctionRepository
import com.bestbidder.usecase.port.PushNotificationPort
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class StartPendingAuctionsUseCase(
    private val auctionRepository: AuctionRepository,
    private val notificationPort: AuctionNotificationPort,
    private val favoriteAuctionRepository: FavoriteAuctionRepository,
    private val pushNotificationPort: PushNotificationPort,
) {
    fun execute(now: Instant) {
        val startingAuctions = auctionRepository.getJustStarted(now)

        if (startingAuctions.isNotEmpty()) {
            println("[NOTIFICACIÓN] Activando ${startingAuctions.size} subasta(s) para el instante: $now")
        }
        startingAuctions.forEach { auction ->
            val cardName = auction.card.name
            notificationPort.notifyAuctionStarted(auction.id)

            pushNotificationPort.send(
                listOf(auction.creatorId),
                PushMessages.ownAuctionStarted(auction.id, cardName),
            )

            val favoriteUserIds = favoriteAuctionRepository.findUserIdsByAuctionId(auction.id)
            pushNotificationPort.send(
                favoriteUserIds,
                PushMessages.favoriteStarted(auction.id, cardName),
            )
        }
    }
}
