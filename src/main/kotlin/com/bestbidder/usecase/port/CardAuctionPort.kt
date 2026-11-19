package com.bestbidder.usecase.port

import java.util.UUID

data class ActiveCardAuction(
    val id: UUID,
    val status: String,
)

interface CardAuctionPort {
    fun findActiveOrUpcomingByCardId(cardId: UUID): ActiveCardAuction?
}
