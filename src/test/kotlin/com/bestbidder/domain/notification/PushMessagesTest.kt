package com.bestbidder.domain.notification

import com.bestbidder.domain.bid.Bid
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.UUID

class PushMessagesTest {
    private val auctionId = UUID.randomUUID()

    @Test
    @DisplayName("El mensaje de ganador incluye la carta, el importe y el id de la subasta")
    fun `auction won message`() {
        val message = PushMessages.auctionWon(auctionId, "Mewtwo", 75.0)

        assertEquals(NotificationType.AUCTION_WON, message.type)
        assertTrue(message.body.contains("Mewtwo"))
        assertTrue(message.body.contains("75,00"))
        assertEquals(auctionId.toString(), message.data["auctionId"])
    }

    @Test
    @DisplayName("El mensaje de fin de subasta indica ganador e importe cuando hubo pujas")
    fun `own auction ended with winner`() {
        val bid = Bid(auctionId = auctionId, bidderId = UUID.randomUUID(), bidderUsername = "carlos", amount = 75.0)

        val message = PushMessages.ownAuctionEnded(auctionId, "Mewtwo", bid)

        assertTrue(message.body.contains("carlos"))
        assertTrue(message.body.contains("75,00"))
    }

    @Test
    @DisplayName("El mensaje de fin de subasta indica que no hubo pujas")
    fun `own auction ended without bids`() {
        val message = PushMessages.ownAuctionEnded(auctionId, "Mewtwo", null)

        assertTrue(message.body.contains("sin pujas"))
    }

    @Test
    @DisplayName("El mensaje de nuevo seguidor lleva el id del seguidor")
    fun `new follower message`() {
        val followerId = UUID.randomUUID()

        val message = PushMessages.newFollower(followerId, "paula")

        assertTrue(message.body.contains("paula"))
        assertEquals(followerId.toString(), message.data["userId"])
    }

    @Test
    @DisplayName("El mensaje de puja en favorita incluye la carta y el importe, y no revela quién pujó")
    fun `favorite new bid message`() {
        val message = PushMessages.favoriteNewBid(auctionId, "Mewtwo", 75.0)

        assertEquals(NotificationType.FAVORITE_NEW_BID, message.type)
        assertTrue(message.body.contains("Mewtwo"))
        assertTrue(message.body.contains("75,00"))
        assertEquals(auctionId.toString(), message.data["auctionId"])
    }
}
