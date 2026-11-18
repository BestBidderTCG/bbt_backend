package com.bestbidder.domain.auction

import com.bestbidder.domain.card.CardState
import java.time.Duration
import java.time.Instant
import java.util.UUID
import kotlin.math.ceil

private const val SECONDS_PER_DAY = 86_400.0
private const val MAX_AUCTION_DAYS = 7

data class AuctionSummary(
    val id: UUID,
    val cardName: String,
    val cardImage: String,
    val currentBid: Double,
    val remainingDays: Int,
    val cardState: CardState,
    val cardCategory: String,
    val cardCollection: String?,
)

fun remainingDaysUntil(
    endsAt: Instant,
    now: Instant,
): Int {
    val seconds = Duration.between(now, endsAt).seconds
    if (seconds <= 0) return 0
    return ceil(seconds / SECONDS_PER_DAY).toInt().coerceIn(0, MAX_AUCTION_DAYS)
}
