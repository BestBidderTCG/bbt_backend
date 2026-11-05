package com.bestbidder.adapter.web.auction.dto

import com.bestbidder.domain.auction.Auction
import com.bestbidder.domain.auction.AuctionStatus
import com.bestbidder.domain.card.Card
import java.time.Instant
import java.util.UUID

data class AuctionResponse(
    val id: UUID,
    val creator_id: UUID,
    val creator_username: String,
    val card: Card,
    val description: String?,
    val min_prize: Double,
    val startedAt: Instant,
    val endsAt: Instant,
    val durationInDays: Int,
    val status: AuctionStatus,
) {
    companion object {
        fun fromDomain(auction: Auction): AuctionResponse =
            AuctionResponse(
                id = auction.id,
                creator_id = auction.creator_id,
                creator_username = auction.creator_username,
                card = auction.card,
                description = auction.description,
                min_prize = auction.min_prize,
                startedAt = auction.startedAt,
                endsAt = auction.endsAt,
                durationInDays = auction.durationInDays,
                status = auction.status,
            )
    }
}
