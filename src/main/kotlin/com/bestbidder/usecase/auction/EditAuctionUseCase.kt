package com.bestbidder.usecase.auction

import com.bestbidder.domain.auction.Auction
import com.bestbidder.domain.notification.Audience
import com.bestbidder.domain.notification.SseEvents
import com.bestbidder.usecase.port.AuctionRepository
import com.bestbidder.usecase.port.RealtimeEventPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class EditAuctionUseCase(
    private val auctionRepository: AuctionRepository,
    private val realtimeEventPort: RealtimeEventPort,
) {
    @Transactional
    fun edit(command: EditAuctionCommand): Auction {
        // Bloqueo de escritura para que el scheduler no la inicie mientras se edita
        val auction = auctionRepository.getByIdForUpdate(command.auctionId)
        requireNotNull(auction) { "Subasta ${command.auctionId} no encontrada" }

        require(auction.creatorId == command.requesterUserId) {
            "No tienes permiso para editar esta subasta"
        }

        val now = Instant.now()
        check(auction.startedAt.isAfter(now)) {
            "Solo se pueden editar subastas que aún no han empezado"
        }

        validateChanges(command, now)

        val updated =
            auction.copy(
                description = command.description ?: auction.description,
                minPrize = command.minPrize ?: auction.minPrize,
                startedAt = command.startedAt ?: auction.startedAt,
                durationInDays = command.durationInDays ?: auction.durationInDays,
            )

        val saved = auctionRepository.save(updated)
        realtimeEventPort.publish(SseEvents.auctionUpdated(saved.id), Audience.Everyone)
        return saved
    }

    private fun validateChanges(
        command: EditAuctionCommand,
        now: Instant,
    ) {
        command.minPrize?.let { require(it > 0) { "El precio mínimo debe ser mayor que 0" } }
        command.durationInDays?.let { require(it > 0) { "La duración debe ser de al menos 1 día" } }
        command.startedAt?.let { require(it.isAfter(now)) { "La fecha de inicio debe ser futura" } }
    }
}
