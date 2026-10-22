package com.bestbidder.domain.auction

import java.util.UUID

/**
 * Representa la entidad de negocio de una Subasta.
 */
data class Auction(
    val id: UUID,
    val creator: String,
    // Más adelante se usará la entidad Card para enlazar la subasta con el prodcuto
    val card: String,
    val collection: String,
    val min_prize: Double,
)
