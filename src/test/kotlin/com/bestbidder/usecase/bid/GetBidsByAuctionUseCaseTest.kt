package com.bestbidder.usecase.bid

import com.bestbidder.domain.bid.Bid
import com.bestbidder.usecase.port.BidRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.util.UUID

class GetBidsByAuctionUseCaseTest {
    private val bidRepository: BidRepository = mock(BidRepository::class.java)
    private val useCase = GetBidsByAuctionUseCase(bidRepository)

    @Test
    @DisplayName("Debe devolver las pujas de una subasta ordenadas de mayor a menor")
    fun `should return bids for a given auction`() {
        val auctionId = UUID.randomUUID()
        val bid1 = Bid(auctionId = auctionId, bidderId = UUID.randomUUID(), bidderUsername = "user1", amount = 200.0)
        val bid2 = Bid(auctionId = auctionId, bidderId = UUID.randomUUID(), bidderUsername = "user2", amount = 150.0)

        `when`(bidRepository.findByAuctionIdOrderByAmountDesc(auctionId)).thenReturn(listOf(bid1, bid2))

        val result = useCase.execute(auctionId)

        assertEquals(2, result.size)
        assertEquals(200.0, result[0].amount)
        assertEquals(150.0, result[1].amount)
        verify(bidRepository, times(1)).findByAuctionIdOrderByAmountDesc(auctionId)
    }
}
