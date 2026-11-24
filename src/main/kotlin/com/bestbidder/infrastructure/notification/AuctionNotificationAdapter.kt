package com.bestbidder.infrastructure.notification

import com.bestbidder.domain.bid.Bid
import com.bestbidder.infrastructure.notification.dto.AuctionNotificationMessage
import com.bestbidder.usecase.port.AuctionNotificationPort
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList

@Component
class AuctionNotificationAdapter : AuctionNotificationPort {
    private val emitters = CopyOnWriteArrayList<SseEmitter>()

    fun addEmitter(emitter: SseEmitter) {
        emitters.add(emitter)

        emitter.onCompletion { emitters.remove(emitter) }
        emitter.onTimeout { emitters.remove(emitter) }
        emitter.onError { emitters.remove(emitter) }
    }

    override fun notifyAuctionStarted(auctionId: UUID) {
        val message =
            AuctionNotificationMessage(
                status = "ACTIVE",
                auctionId = auctionId,
            )
        sendToFrontend(message)
    }

    override fun notifyAuctionExpired(
        auctionId: UUID,
        winningBid: Bid?,
    ) {
        val message =
            AuctionNotificationMessage(
                status = "EXPIRED",
                auctionId = auctionId,
                amount = winningBid?.amount,
                bidderUsername = winningBid?.bidderUsername,
            )
        sendToFrontend(message)
    }

    override fun notifyAuctionCancelled(auctionId: UUID) {
        val message =
            AuctionNotificationMessage(
                status = "CANCELLED",
                auctionId = auctionId,
            )
        sendToFrontend(message)
    }

    override fun notifyNewBidPlaced(
        auctionId: UUID,
        newAmount: Double,
        bidderUsername: String,
    ) {
        val message =
            AuctionNotificationMessage(
                status = "BID_PLACED",
                auctionId = auctionId,
                amount = newAmount,
                bidderUsername = bidderUsername,
            )
        sendToFrontend(message)
    }

    /**
     * Envía la notificación SSE a los clientes conectados.
     *
     * Si existe una transacción de base de datos activa (por ejemplo, al registrar
     * una puja), pospone el envío hasta que se confirme el COMMIT (afterCommit).
     * Esto evita dos problemas:
     * 1. Que el frontend consulte el historial antes de que la puja sea visible en BD.
     * 2. Que se notifique una acción cuya transacción acabe haciendo ROLLBACK por error.
     */
    private fun sendToFrontend(message: AuctionNotificationMessage) {
        val emitAction = {
            val deadEmitters = mutableListOf<SseEmitter>()

            emitters.forEach { emitter ->
                runCatching {
                    emitter.send(
                        SseEmitter.event()
                            .name("auction-status-change")
                            .data(message),
                    )
                }.onFailure {
                    deadEmitters.add(emitter)
                }
            }

            emitters.removeAll(deadEmitters)
            Unit
        }

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                object : TransactionSynchronization {
                    override fun afterCommit() {
                        emitAction()
                    }
                },
            )
        } else {
            emitAction()
        }
    }
}
