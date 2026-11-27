package com.bestbidder.domain.notification

import com.bestbidder.domain.bid.Bid
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.UUID

class SseEventsTest {
    private val auctionId = UUID.randomUUID()
    private val userId = UUID.randomUUID()

    @Test
    @DisplayName("AUCTION_CREATED lleva la subasta y su creador")
    fun `auction created`() {
        val event = SseEvents.auctionCreated(auctionId, userId)

        assertEquals(SseEventType.AUCTION_CREATED, event.type)
        assertEquals(mapOf("auctionId" to auctionId, "creatorId" to userId), event.data)
    }

    @Test
    @DisplayName("AUCTION_UPDATED y AUCTION_STARTED solo llevan el id de la subasta")
    fun `updated and started carry only the auction id`() {
        assertEquals(SseEventType.AUCTION_UPDATED, SseEvents.auctionUpdated(auctionId).type)
        assertEquals(mapOf("auctionId" to auctionId), SseEvents.auctionUpdated(auctionId).data)
        assertEquals(SseEventType.AUCTION_STARTED, SseEvents.auctionStarted(auctionId).type)
        assertEquals(mapOf("auctionId" to auctionId), SseEvents.auctionStarted(auctionId).data)
    }

    @Test
    @DisplayName("AUCTION_ENDED incluye ganador e importe cuando hubo pujas")
    fun `auction ended with winner`() {
        val bid = Bid(auctionId = auctionId, bidderId = userId, bidderUsername = "carlos", amount = 75.0)

        val event = SseEvents.auctionEnded(auctionId, bid)

        assertEquals(SseEventType.AUCTION_ENDED, event.type)
        assertEquals("carlos", event.data["winnerUsername"])
        assertEquals(75.0, event.data["finalAmount"])
    }

    @Test
    @DisplayName("AUCTION_ENDED sin pujas no lleva ganador ni importe")
    fun `auction ended without bids`() {
        val event = SseEvents.auctionEnded(auctionId, null)

        assertEquals(auctionId, event.data["auctionId"])
        assertNull(event.data["winnerUsername"])
        assertNull(event.data["finalAmount"])
    }

    @Test
    @DisplayName("BID_PLACED lleva el importe y el pujador")
    fun `bid placed`() {
        val event = SseEvents.bidPlaced(auctionId, 155.0, "pujador2")

        assertEquals(SseEventType.BID_PLACED, event.type)
        assertEquals(mapOf("auctionId" to auctionId, "amount" to 155.0, "bidderUsername" to "pujador2"), event.data)
    }

    @Test
    @DisplayName("FOLLOWERS_CHANGED indica quién ha cambiado, si sigue o no y el nuevo contador")
    fun `followers changed`() {
        val followerId = UUID.randomUUID()

        val event = SseEvents.followersChanged(userId, followerId, followed = false, followersCount = 4L)

        assertEquals(SseEventType.FOLLOWERS_CHANGED, event.type)
        assertEquals(
            mapOf("userId" to userId, "followerId" to followerId, "followed" to false, "followersCount" to 4L),
            event.data,
        )
    }

    @Test
    @DisplayName("BALANCE_CHANGED lleva el nuevo saldo y la subasta que lo ha provocado")
    fun `balance changed`() {
        val event = SseEvents.balanceChanged(auctionId, 200.0)

        assertEquals(SseEventType.BALANCE_CHANGED, event.type)
        assertEquals(mapOf("auctionId" to auctionId, "balance" to 200.0), event.data)
    }
}
