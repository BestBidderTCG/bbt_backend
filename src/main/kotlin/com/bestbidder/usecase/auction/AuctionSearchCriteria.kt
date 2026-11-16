package com.bestbidder.usecase.auction

import java.math.BigDecimal
import java.util.UUID

private const val DEFAULT_PAGE = 0
private const val DEFAULT_SIZE = 20

data class AuctionSearchCriteria(
    val query: String? = null,
    val onlyActive: Boolean? = null,
    val creator: String? = null,
    val cardId: UUID? = null,
    val minPrice: BigDecimal? = null,
    val maxPrice: BigDecimal? = null,
    val page: Int = DEFAULT_PAGE,
    val size: Int = DEFAULT_SIZE,
    val sortBy: String = "createdAt",
    val sortDirection: String = "DESC",
)
