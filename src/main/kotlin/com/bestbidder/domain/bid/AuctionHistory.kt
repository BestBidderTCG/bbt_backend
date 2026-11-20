package com.bestbidder.domain.bid

import java.util.UUID

data class AuctionHistory(
    val bidderId: UUID,
    val bidderName: String,
    val bidderAvatar: String,
    val amount: Double,
)
