package com.bestbidder.usecase.auction

import com.bestbidder.usecase.port.AuctionRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class CancelAuctionUseCase(
    private val auctionRepository: AuctionRepository,
) {
    fun cancel(
        auctionId: UUID,
        requesterUserId: UUID,
    ) {
        val auction =
            auctionRepository.getById(auctionId)
                ?: throw Exception("Subasta $auctionId no encontrada")

        // Validar que el usuario tenga permisos (owner o admin)
        if (auction.creator_id != requesterUserId) {
            throw Exception("No tienes permiso para cancelar esta subasta")
        }

        auction.cancel()
        auctionRepository.save(auction)

        // Notificar a los pujadores si correspondiera
        // if (auction.hasBids()) {
        //    ...
        // }
    }
}
