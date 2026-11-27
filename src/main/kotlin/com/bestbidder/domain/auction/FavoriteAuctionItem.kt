package com.bestbidder.domain.auction

data class FavoriteAuctionItem(
    val auction: AuctionListItem,
    val notificationMode: NotificationMode,
)
