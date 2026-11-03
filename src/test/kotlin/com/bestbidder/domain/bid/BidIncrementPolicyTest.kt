package com.bestbidder.domain.bid

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class BidIncrementPolicyTest {
    @Test
    @DisplayName("Debe retornar el minPrize si no hay pujas previas")
    fun `should return minPrize when there are no previous bids`() {
        val next = BidIncrementPolicy.calculateMinimumNextBid(minPrize = 10.0, currentHighestBid = null)
        assertEquals(10.0, next)
    }

    @Test
    @DisplayName("Debe aplicar 0.10 de incremento para pujas menores a 1 euro")
    fun `should apply 0_10 increment for bids under 1 euro`() {
        val next = BidIncrementPolicy.calculateMinimumNextBid(minPrize = 0.50, currentHighestBid = 0.50)
        assertEquals(0.60, next)
    }

    @Test
    @DisplayName("Debe aplicar 0.50 de incremento para pujas entre 5 y 25 euros")
    fun `should apply 0_50 increment for bids between 5 and 25 euros`() {
        val next = BidIncrementPolicy.calculateMinimumNextBid(minPrize = 1.0, currentHighestBid = 12.0)
        assertEquals(12.50, next)
    }

    @Test
    @DisplayName("Debe aplicar 5.00 de incremento para pujas entre 100 y 250 euros")
    fun `should apply 5_00 increment for bids between 100 and 250 euros`() {
        val next = BidIncrementPolicy.calculateMinimumNextBid(minPrize = 10.0, currentHighestBid = 150.0)
        assertEquals(155.0, next)
    }
}
