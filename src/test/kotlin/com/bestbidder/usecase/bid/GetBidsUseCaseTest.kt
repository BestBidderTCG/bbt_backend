package com.bestbidder.usecase.bid

import com.bestbidder.domain.auction.AuctionStatus
import com.bestbidder.domain.bid.AuctionHistory
import com.bestbidder.domain.bid.BidderHistory
import com.bestbidder.usecase.port.BidRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.util.UUID

class GetBidsUseCaseTest {
    private val bidRepository: BidRepository = mock(BidRepository::class.java)
    private val useCase = GetBidsUseCase(bidRepository)

    @Nested
    @DisplayName("byAuction")
    inner class ByAuctionTests {
        @Test
        @DisplayName("Debe devolver el historial de pujas de una subasta ordenado de mayor a menor")
        fun `should return bid history for a given auction`() {
            val auctionId = UUID.randomUUID()
            val h1 =
                AuctionHistory(
                    bidderId = UUID.randomUUID(),
                    bidderName = "user1",
                    bidderAvatar = "a1.png",
                    amount = 200.0,
                )
            val h2 =
                AuctionHistory(
                    bidderId = UUID.randomUUID(),
                    bidderName = "user2",
                    bidderAvatar = "a2.png",
                    amount = 150.0,
                )

            `when`(bidRepository.getBidHistoryByAuction(auctionId)).thenReturn(listOf(h1, h2))

            val result = useCase.byAuction(auctionId)

            assertEquals(2, result.size)
            assertEquals(200.0, result[0].amount)
            assertEquals("user1", result[0].bidderName)
            assertEquals(150.0, result[1].amount)
            verify(bidRepository, times(1)).getBidHistoryByAuction(auctionId)
        }

        @Test
        @DisplayName("Debe devolver lista vacía si la subasta no tiene pujas")
        fun `should return empty list when auction has no bids`() {
            val auctionId = UUID.randomUUID()
            `when`(bidRepository.getBidHistoryByAuction(auctionId)).thenReturn(emptyList())

            val result = useCase.byAuction(auctionId)

            assertEquals(0, result.size)
        }
    }

    @Nested
    @DisplayName("byBidder")
    inner class ByBidderTests {
        @Test
        @DisplayName("Debe devolver el historial de pujas de un usuario")
        fun `should return bid history for a given bidder`() {
            val bidderId = UUID.randomUUID()
            val h1 =
                BidderHistory(
                    auctionId = UUID.randomUUID(),
                    cardName = "Charizard",
                    cardImage = "charizard.png",
                    amount = 300.0,
                    status = AuctionStatus.IN_PROGRESS,
                    iAmWinning = true,
                )
            val h2 =
                BidderHistory(
                    auctionId = UUID.randomUUID(),
                    cardName = "Pikachu",
                    cardImage = "pikachu.png",
                    amount = 50.0,
                    status = AuctionStatus.CANCELLED,
                    iAmWinning = null,
                )

            `when`(bidRepository.getBidHistoryByUser(bidderId)).thenReturn(listOf(h1, h2))

            val result = useCase.byBidder(bidderId)

            assertEquals(2, result.size)
            assertEquals("Charizard", result[0].cardName)
            assertEquals(true, result[0].iAmWinning)
            assertEquals(AuctionStatus.CANCELLED, result[1].status)
            assertNull(result[1].iAmWinning)
            verify(bidRepository, times(1)).getBidHistoryByUser(bidderId)
        }
    }
}
