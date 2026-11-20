package com.bestbidder.usecase.auction

import java.time.Instant
import java.util.UUID

data class EditAuctionCommand(
    val auctionId: UUID,
    val requesterUserId: UUID,
    val description: String? = null,
    val minPrize: Double? = null,
    val startedAt: Instant? = null,
    val durationInDays: Int? = null,
)
