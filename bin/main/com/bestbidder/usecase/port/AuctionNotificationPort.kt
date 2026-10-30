package com.bestbidder.usecase.port

import java.util.UUID

interface AuctionNotificationPort {
    fun notifyAuctionStarted(auctionId: UUID)

    fun notifyAuctionExpired(auctionId: UUID)
}
