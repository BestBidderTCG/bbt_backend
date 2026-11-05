package com.bestbidder.adapter.web.card.dto

import com.bestbidder.domain.card.CardState

data class CreateCardRequest(
    val name: String,
    val image: String,
    val category: String,
    val collection: String? = null,
    val num: Int? = null,
    val state: CardState,
    val description: String? = null,
)
