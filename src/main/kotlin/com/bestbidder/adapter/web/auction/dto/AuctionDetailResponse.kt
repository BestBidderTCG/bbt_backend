package com.bestbidder.adapter.web.auction.dto

import com.bestbidder.domain.auction.AuctionDetail
import com.bestbidder.domain.auction.AuctionStatus
import com.bestbidder.domain.card.CardState
import java.util.UUID

data class AuctionCreatorResponse(
    val id: UUID,
    val username: String,
    val avatarNum: Int?,
)

data class AuctionDetailResponse(
    val id: UUID,
    val status: AuctionStatus,
    val cardName: String,
    val cardImage: String,
    val cardCollection: String?,
    val cardNumber: Int?,
    val cardState: CardState,
    val minPrize: Double,
    val increment: Double,
    val lastBid: Double?,
    val bidCount: Long,
    val startedAt: Long,
    val endsAt: Long,
    val description: String?,
    val creator: AuctionCreatorResponse?,
    val isFavorite: Boolean?,
) {
    companion object {
        fun fromDomain(d: AuctionDetail) =
            AuctionDetailResponse(
                id = d.id,
                status = d.status,
                cardName = d.cardName,
                cardImage = d.cardImage,
                cardCollection = d.cardCollection,
                cardNumber = d.cardNumber,
                cardState = d.cardState,
                minPrize = d.minPrize,
                increment = d.increment,
                lastBid = d.lastBid,
                bidCount = d.bidCount,
                startedAt = d.startedAt.toEpochMilli(),
                endsAt = d.endsAt.toEpochMilli(),
                description = d.description,
                creator = d.creator?.let { AuctionCreatorResponse(it.id, it.username, it.avatarNum) },
                isFavorite = d.isFavorite,
            )
    }
}
