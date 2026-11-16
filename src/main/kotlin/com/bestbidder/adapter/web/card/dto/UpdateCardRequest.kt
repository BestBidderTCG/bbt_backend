package com.bestbidder.adapter.web.card.dto

import com.bestbidder.domain.card.CardState

data class UpdateCardRequest(
    val name: String? = null,
    val category: String? = null,
    val collection: String? = null,
    val num: Int? = null,
    val state: CardState? = null,
    val description: String? = null
)