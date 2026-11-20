package com.bestbidder.usecase.port

import java.time.Instant
import java.util.UUID

interface AuctionViewRepository {
    /** Devuelve true si la visita se ha contabilizado. */
    fun registerView(
        auctionId: UUID,
        userId: UUID,
        now: Instant,
    ): Boolean
}
