package com.bestbidder.adapter.web.auction.dto

import com.bestbidder.usecase.auction.AuctionSearchCriteria
import java.math.BigDecimal
import java.util.UUID

private const val DEFAULT_PAGE = 0
private const val DEFAULT_PAGE_SIZE = 20
private const val MAX_PAGE_SIZE = 50

data class SearchAuctionRequest(
    val q: String? = null,
    val active: Boolean? = null,
    val creator: String? = null,
    val cardId: UUID? = null,
    val minPrice: BigDecimal? = null,
    val maxPrice: BigDecimal? = null,
    val page: Int = DEFAULT_PAGE,
    val size: Int = DEFAULT_PAGE_SIZE,
    val sortBy: String = "createdAt",
    val direction: String = "DESC",
) {
    fun toCriteria(): AuctionSearchCriteria =
        AuctionSearchCriteria(
            query = q?.trim()?.takeIf { it.isNotEmpty() },
            onlyActive = active,
            creator = creator,
            cardId = cardId,
            minPrice = minPrice,
            maxPrice = maxPrice,
            page = page.coerceAtLeast(DEFAULT_PAGE),
            size = size.coerceIn(1, MAX_PAGE_SIZE),
            sortBy = sortBy,
            sortDirection = direction,
        )
}
