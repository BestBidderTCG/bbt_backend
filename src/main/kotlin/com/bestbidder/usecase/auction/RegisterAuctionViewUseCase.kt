package com.bestbidder.usecase.auction

import com.bestbidder.usecase.port.AuctionViewRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class RegisterAuctionViewUseCase(
    private val auctionViewRepository: AuctionViewRepository,
) {
    private val log = LoggerFactory.getLogger(RegisterAuctionViewUseCase::class.java)

    @Suppress("TooGenericExceptionCaught")
    fun register(
        auctionId: UUID,
        userId: UUID,
    ) {
        try {
            auctionViewRepository.registerView(auctionId, userId, Instant.now())
        } catch (e: Exception) {
            log.warn("No se pudo registrar la visita de {} a la subasta {}", userId, auctionId, e)
        }
    }
}
