package com.bestbidder.usecase.auction

import com.bestbidder.usecase.port.AuctionNotificationPort
import com.bestbidder.usecase.port.AuctionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class CancelAuctionUseCase(
    private val auctionRepository: AuctionRepository,
    private val notificationPort: AuctionNotificationPort,
) {
    @Transactional
    fun cancel(
        auctionId: UUID,
        requesterUserId: UUID,
    ) {
        val auction =
            auctionRepository.getById(auctionId)
                ?: throw IllegalArgumentException("Subasta $auctionId no encontrada")

        // Validar que el usuario sea el creador
        if (auction.creator_id != requesterUserId) {
            throw IllegalArgumentException("No tienes permiso para cancelar esta subasta")
        }

        if (auction.isCancelled) {
            throw IllegalStateException("La subasta ya se encuentra cancelada")
        }

        val cancelledAuction = auction.cancel()
        auctionRepository.save(cancelledAuction)
        notificationPort.notifyAuctionCancelled(auction.id)
    }
}
