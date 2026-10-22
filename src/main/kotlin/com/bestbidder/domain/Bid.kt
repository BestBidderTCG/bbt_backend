package com.bestbidder.domain.bid

import com.bestbidder.domain.auction.Auction

/**
 * Representa la entidad de negocio de una Puja.
 */
data class Bid(
    val id: String,
    val bidder: String,
    val auction: Auction,
    
)
