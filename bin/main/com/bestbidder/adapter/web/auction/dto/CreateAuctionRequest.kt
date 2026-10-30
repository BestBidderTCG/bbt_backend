package com.bestbidder.adapter.web.auction.dto

import java.time.Instant
import java.util.UUID

/**
 * DTO de entrada para la creación de subastas.
 */
data class CreateAuctionRequest(
    val creator_id: UUID,
    val creator_username: String,
    val card: String,
    val collection: String,
    val min_prize: Double,
    val started_at: Instant,
    val duration_in_days: Int,
)
