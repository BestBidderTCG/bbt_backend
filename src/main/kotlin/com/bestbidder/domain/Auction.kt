package com.bestbidder.domain.auction

import java.util.UUID
import com.bestbidder.domain.cart.Cart

/**
 * Representa la entidad de negocio de una Subasta.
 */
data class Auction(
    val id: UUID,
    val creator: String,
    val cart: Cart,
    val collection: String,
    val min_prize: Double,
    
)