package com.bestbidder.domain.bid

import java.math.BigDecimal
import java.math.RoundingMode

object BidIncrementPolicy {
    /**
     * Incremento aplicable según el importe de referencia (última puja o precio mínimo).
     */
    fun incrementFor(amount: Double): Double =
        when {
            amount < 1.0 -> 0.10
            amount < 5.0 -> 0.25
            amount < 25.0 -> 0.50
            amount < 100.0 -> 1.00
            amount < 250.0 -> 5.00
            amount < 500.0 -> 10.00
            else -> 25.00
        }

    fun calculateMinimumNextBid(
        minPrize: Double,
        currentHighestBid: Double?,
    ): Double {
        if (currentHighestBid == null) {
            return minPrize
        }

        return BigDecimal.valueOf(currentHighestBid + incrementFor(currentHighestBid))
            .setScale(2, RoundingMode.HALF_UP)
            .toDouble()
    }
}
