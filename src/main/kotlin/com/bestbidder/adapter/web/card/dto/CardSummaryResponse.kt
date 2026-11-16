package com.bestbidder.adapter.web.card.dto

import com.bestbidder.domain.card.Card
import java.util.UUID

data class CardSummaryResponse(
    val id: UUID,
    val name: String,
    val category: String,
    val image: String,
) {
    companion object {
        fun fromDomain(card: Card) = CardSummaryResponse(
            id = card.id,
            name = card.name,
            category = card.category,
            image = card.image
        )
    }
}