package com.bestbidder.adapter.web.bid.dto

import java.util.UUID

data class PlaceBidRequest(
    val bidder_id: UUID,
    val bidder_username: String,
    val amount: Double,
)
