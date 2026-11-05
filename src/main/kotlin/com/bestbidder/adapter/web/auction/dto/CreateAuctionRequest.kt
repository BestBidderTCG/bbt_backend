package com.bestbidder.adapter.web.auction.dto

import java.time.Instant
import java.util.UUID

data class CreateAuctionRequest(
    val creator_id: UUID,
    val creator_username: String,
    val card_id: UUID,
    val description: String? = null,
    val min_prize: Double,
    val started_at: Instant,
    val duration_in_days: Int,
)
