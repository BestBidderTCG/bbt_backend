package com.bestbidder.adapter.web.aution.dto

/**
 * DTO  de entrada.
 */
data class CreateAuctionRequest(
    val username: String,
    val card: String,
    val collection: String,
    val min_prize: Double,
)
