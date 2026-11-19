package com.bestbidder.domain.bid

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class BidIncrementPolicyTest {
    @Test
    @DisplayName("incrementFor devuelve el tramo correcto, incluidos los límites")
    fun `should return increment for each tier`() {
        assertEquals(0.10, BidIncrementPolicy.incrementFor(0.99))
        assertEquals(0.25, BidIncrementPolicy.incrementFor(1.0))
        assertEquals(0.50, BidIncrementPolicy.incrementFor(5.0))
        assertEquals(1.00, BidIncrementPolicy.incrementFor(25.0))
        assertEquals(5.00, BidIncrementPolicy.incrementFor(100.0))
        assertEquals(10.00, BidIncrementPolicy.incrementFor(250.0))
        assertEquals(25.00, BidIncrementPolicy.incrementFor(500.0))
    }

    @Test
    @DisplayName("Sin pujas, la puja mínima es el precio mínimo")
    fun `should return min prize when no bids`() {
        assertEquals(80.0, BidIncrementPolicy.calculateMinimumNextBid(80.0, null))
    }

    @Test
    @DisplayName("Con pujas, la puja mínima es la última más su incremento")
    fun `should add increment to highest bid`() {
        assertEquals(97.0, BidIncrementPolicy.calculateMinimumNextBid(10.0, 96.0))
        assertEquals(105.0, BidIncrementPolicy.calculateMinimumNextBid(10.0, 100.0))
        assertEquals(125.0, BidIncrementPolicy.calculateMinimumNextBid(10.0, 120.0))
    }
}
