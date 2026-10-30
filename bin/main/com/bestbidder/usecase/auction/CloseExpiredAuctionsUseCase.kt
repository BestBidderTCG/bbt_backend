package com.bestbidder.usecase.auction

import com.bestbidder.usecase.port.AuctionNotificationPort
import com.bestbidder.usecase.port.AuctionRepository
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class CloseExpiredAuctionsUseCase(
    private val auctionRepository: AuctionRepository,
    private val notificationPort: AuctionNotificationPort,
) {
    fun execute(now: Instant) {
        // Obtiene únicamente las subastas que caducan en este instante
        val expiredAuctions = auctionRepository.getJustExpired(now)

        expiredAuctions.forEach { auction ->
            // Le pide a la interfaz que envíe la notificación de expirada
            notificationPort.notifyAuctionExpired(auction.id)
        }
    }
}
