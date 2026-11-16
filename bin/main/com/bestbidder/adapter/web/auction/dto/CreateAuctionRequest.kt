package com.bestbidder.adapter.web.auction.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant
import java.util.UUID

data class CreateAuctionRequest(
    @JsonProperty("card_id")
    val cardId: UUID,
    val description: String? = null,
    @JsonProperty("min_prize")
    val minPrize: Double,
    @JsonProperty("started_at")
    val startedAt: Instant,
    @JsonProperty("duration_in_days")
    val durationInDays: Int,
)
