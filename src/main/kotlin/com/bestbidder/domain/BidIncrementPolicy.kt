package com.bestbidder.domain.bid

import java.math.BigDecimal
import java.math.RoundingMode

object BidIncrementPolicy {
    fun calculateMinimumNextBid(
        minPrize: Double,
        currentHighestBid: Double?,
    ): Double {
        if (currentHighestBid == null) {
            return minPrize
        }

        val increment =
            when {
                currentHighestBid < 1.0 -> 0.10
                currentHighestBid < 5.0 -> 0.25
                currentHighestBid < 25.0 -> 0.50
                currentHighestBid < 100.0 -> 1.00
                currentHighestBid < 250.0 -> 5.00
                currentHighestBid < 500.0 -> 10.00
                else -> 25.00
            }

        return BigDecimal(currentHighestBid + increment)
            .setScale(2, RoundingMode.HALF_UP)
            .toDouble()
    }
}
