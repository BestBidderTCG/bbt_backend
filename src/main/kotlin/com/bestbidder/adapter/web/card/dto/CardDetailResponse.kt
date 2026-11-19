package com.bestbidder.adapter.web.card.dto

import com.bestbidder.domain.card.Card
import com.bestbidder.domain.card.CardState
import com.bestbidder.usecase.port.ActiveCardAuction
import java.time.Instant
import java.util.UUID

data class CardAuctionResponse(
    val id: UUID,
    val status: String,
)

@Suppress("LongParameterList")
data class CardDetailResponse(
    val id: UUID,
    val ownerId: UUID,
    val name: String,
    val image: String,
    val category: String,
    val collection: String?,
    val num: String?,
    val state: CardState,
    val description: String?,
    val createdAt: Instant,
    val auction: CardAuctionResponse?,
) {
    companion object {
        fun fromDomain(
            card: Card,
            auction: ActiveCardAuction?,
        ) = CardDetailResponse(
            id = card.id,
            ownerId = card.ownerId,
            name = card.name,
            image = card.image,
            category = card.category,
            collection = card.collection,
            num = card.num?.toString(),
            state = card.state,
            description = card.description,
            createdAt = card.createdAt,
            auction = auction?.let { CardAuctionResponse(id = it.id, status = it.status) },
        )
    }
}
