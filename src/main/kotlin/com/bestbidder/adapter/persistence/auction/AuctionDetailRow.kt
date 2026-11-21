package com.bestbidder.adapter.persistence.auction

import com.bestbidder.domain.auction.AuctionCreator
import com.bestbidder.domain.auction.AuctionDetail
import com.bestbidder.domain.auction.AuctionStatus
import com.bestbidder.domain.bid.BidIncrementPolicy
import com.bestbidder.domain.card.CardState
import java.time.Instant
import java.util.UUID

data class AuctionDetailRow(
    val id: UUID,
    val cardName: String,
    val cardImage: String,
    val cardCollection: String?,
    val cardNumber: Int?,
    val cardState: CardState,
    val minPrize: Double,
    val lastBid: Double?,
    val bidCount: Long,
    val startedAt: Instant,
    val endsAt: Instant,
    val description: String?,
    val creatorId: UUID,
    val creatorUsername: String,
    val creatorAvatarNum: Int?,
    val isFavorite: Boolean,
) {
    fun toDetail(
        now: Instant,
        viewerId: UUID?,
    ): AuctionDetail {
        val status =
            when {
                !endsAt.isAfter(now) -> AuctionStatus.FINISHED
                startedAt.isAfter(now) -> AuctionStatus.UPCOMING
                else -> AuctionStatus.IN_PROGRESS
            }
        val isOwner = viewerId != null && viewerId == creatorId

        return AuctionDetail(
            id = id,
            status = status,
            cardName = cardName,
            cardImage = cardImage,
            cardCollection = cardCollection,
            cardNumber = cardNumber,
            cardState = cardState,
            minPrize = minPrize,
            increment = BidIncrementPolicy.incrementFor(lastBid ?: minPrize),
            lastBid = lastBid,
            bidCount = bidCount,
            startedAt = startedAt,
            endsAt = endsAt,
            description = description,
            creator = if (isOwner) null else AuctionCreator(creatorId, creatorUsername, creatorAvatarNum),
            isFavorite = if (viewerId == null || isOwner) null else isFavorite,
        )
    }
}
