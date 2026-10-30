package com.bestbidder.adapter.web.auction.dto

import com.bestbidder.domain.auction.Auction
import com.bestbidder.domain.auction.AuctionStatus
import java.time.Instant
import java.util.UUID

data class AuctionResponse(
    val id: UUID,
    val creator_id: UUID,
    val creator_username: String,
    val card: String,
    val collection: String,
    val min_prize: Double,
    val started_at: Instant,
    val duration_in_days: Int,
    val ends_at: Instant,
    val is_cancelled: Boolean,
    val status: AuctionStatus,
) {
    companion object {
        fun fromDomain(domain: Auction): AuctionResponse {
            return AuctionResponse(
                id = domain.id,
                creator_id = domain.creator_id,
                creator_username = domain.creator_username,
                card = domain.card,
                collection = domain.collection,
                min_prize = domain.min_prize,
                started_at = domain.startedAt,
                duration_in_days = domain.durationInDays,
                ends_at = domain.endsAt,
                is_cancelled = domain.isCancelled,
                status = domain.getStatusAt(Instant.now()),
            )
        }
    }
}
