package com.bestbidder.usecase.auction

import com.bestbidder.domain.auction.NotificationMode
import com.bestbidder.domain.notification.Audience
import com.bestbidder.domain.notification.PushMessages
import com.bestbidder.domain.notification.SseEvents
import com.bestbidder.usecase.port.AuctionRepository
import com.bestbidder.usecase.port.FavoriteAuctionRepository
import com.bestbidder.usecase.port.PushNotificationPort
import com.bestbidder.usecase.port.RealtimeEventPort
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class StartPendingAuctionsUseCase(
    private val auctionRepository: AuctionRepository,
    private val realtimeEventPort: RealtimeEventPort,
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
            realtimeEventPort.publish(SseEvents.auctionStarted(auction.id), Audience.Everyone)

            pushNotificationPort.send(
                listOf(auction.creatorId),
                PushMessages.ownAuctionStarted(auction.id, cardName),
            )

            val favoriteUserIds =
                favoriteAuctionRepository.findUserIdsByAuctionId(auction.id, NotificationMode.LIFECYCLE)
            pushNotificationPort.send(
                favoriteUserIds,
                PushMessages.favoriteStarted(auction.id, cardName),
            )
        }
    }
}
