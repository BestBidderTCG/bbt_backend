package com.bestbidder.infrastructure.notification.dto

import java.util.UUID

data class AuctionNotificationMessage(
    val status: String,
    val auctionId: UUID,
)
