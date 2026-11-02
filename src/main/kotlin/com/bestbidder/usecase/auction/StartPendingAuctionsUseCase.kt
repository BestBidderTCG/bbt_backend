package com.bestbidder.usecase.auction

import com.bestbidder.usecase.port.AuctionNotificationPort
import com.bestbidder.usecase.port.AuctionRepository
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class StartPendingAuctionsUseCase(
    private val auctionRepository: AuctionRepository,
    private val notificationPort: AuctionNotificationPort,
) {
    fun execute(now: Instant) {
        // Ejecuta la consulta optimizada en BD pasando el tiempo truncado
        val startingAuctions = auctionRepository.getJustStarted(now)

        if (startingAuctions.isNotEmpty()) {
            println("[NOTIFICACIÓN] Activando ${startingAuctions.size} subasta(s) para el instante: $now")
        }
        startingAuctions.forEach { auction ->
            // Le pide a la interfaz que envíe la notificación de activación
            notificationPort.notifyAuctionStarted(auction.id)
        }
    }
}
