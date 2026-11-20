package com.bestbidder.adapter.web.auction.dto

import java.time.Instant

data class EditAuctionRequest(
    val description: String? = null,
    val minPrize: Double? = null,
    val startedAt: Instant? = null,
    val durationInDays: Int? = null,
)
