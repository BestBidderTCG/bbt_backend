package com.bestbidder.adapter.web.wallet.dto

import jakarta.validation.constraints.Positive

data class TopUpRequest(
    @field:Positive
    val amount: Double,
)
