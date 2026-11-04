package com.bestbidder.domain.bid

import java.time.Instant
import java.util.UUID

data class Bid(
    val id: UUID = UUID.randomUUID(),
    val auctionId: UUID,
    val bidderId: UUID,
    val bidderUsername: String,
    val amount: Double,
    val timestamp: Instant = Instant.now(),
) {
    init {
        require(amount > 0.0) { "El monto de la puja debe ser mayor a 0" }
    }
}
