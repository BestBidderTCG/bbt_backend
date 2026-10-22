package com.bestbidder.adapter.web.auction.dto

import com.bestbidder.domain.auction.Auction

data class AuctionResponse(
    val id: String,
    val creator: String,
    val card: String,
    val collection: String,
    val minPrize: Double,
) {
    companion object {
        fun fromDomain(auction: Auction): AuctionResponse {
            return AuctionResponse(
                id = auction.id.toString(),
                creator = auction.creator,
                card = auction.card,
                collection = auction.collection,
                minPrize = auction.min_prize,
            )
        }
    }
}
