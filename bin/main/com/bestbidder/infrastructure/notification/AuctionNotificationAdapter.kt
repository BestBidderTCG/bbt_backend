package com.bestbidder.infrastructure.notification

import com.bestbidder.infrastructure.notification.dto.AuctionNotificationMessage
import com.bestbidder.usecase.port.AuctionNotificationPort
import org.springframework.stereotype.Component
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

    override fun notifyAuctionExpired(auctionId: UUID) {
        val message =
            AuctionNotificationMessage(
                status = "EXPIRED",
                auctionId = auctionId,
            )
        sendToFrontend(message)
    }

    private fun sendToFrontend(message: AuctionNotificationMessage) {
        val deadEmitters = mutableListOf<SseEmitter>()

        emitters.forEach { emitter ->
            try {
                emitter.send(
                    SseEmitter.event()
                        .name("auction-status-change")
                        .data(message),
                )
            } catch (e: Exception) {
                deadEmitters.add(emitter)
            }
        }

        emitters.removeAll(deadEmitters)
    }
}
