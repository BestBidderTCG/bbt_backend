package com.bestbidder.adapter.web.auction.dto

/**
 * DTO  de entrada.
 */
data class CreateAuctionRequest(
    val creator: String,
    val card: String,
    val collection: String,
    val min_prize: Double,
    // Habría que añadir un campo con fecha para marcar cuando termina la subasta
)
