package com.bestbidder.adapter.web.card.dto

import com.bestbidder.domain.card.Card
import java.util.UUID

data class CardSummaryResponse(
    val id: UUID,
    val name: String,
    val image: String,
    val category: String,
    val inAuction: Boolean,
) {
    companion object {
        fun fromDomain(
            card: Card,
            inAuction: Boolean,
        ) = CardSummaryResponse(
            id = card.id,
            name = card.name,
            image = card.image,
            category = card.category,
            inAuction = inAuction,
        )
    }
}
