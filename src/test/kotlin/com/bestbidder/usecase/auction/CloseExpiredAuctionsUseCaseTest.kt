package com.bestbidder.usecase.auction

import com.bestbidder.domain.auction.Auction
import com.bestbidder.domain.bid.Bid
import com.bestbidder.domain.card.Card
import com.bestbidder.domain.card.CardState
import com.bestbidder.domain.notification.PushMessages
import com.bestbidder.usecase.port.AuctionNotificationPort
import com.bestbidder.usecase.port.AuctionRepository
import com.bestbidder.usecase.port.BidRepository
import com.bestbidder.usecase.port.FavoriteAuctionRepository
import com.bestbidder.usecase.port.PushNotificationPort
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

class CloseExpiredAuctionsUseCaseTest {
    private val auctionRepository: AuctionRepository = mock(AuctionRepository::class.java)
    private val bidRepository: BidRepository = mock(BidRepository::class.java)
    private val notificationPort: AuctionNotificationPort = mock(AuctionNotificationPort::class.java)
    private val favoriteAuctionRepository: FavoriteAuctionRepository = mock(FavoriteAuctionRepository::class.java)
    private val pushNotificationPort: PushNotificationPort = mock(PushNotificationPort::class.java)
    private val useCase =
        CloseExpiredAuctionsUseCase(
            auctionRepository,
            bidRepository,
            notificationPort,
            favoriteAuctionRepository,
            pushNotificationPort,
        )

    private val now = Instant.now().truncatedTo(ChronoUnit.MINUTES)
    private val auctionId = UUID.randomUUID()
    private val creatorId = UUID.randomUUID()

    private val auction =
        Auction(
            id = auctionId,
            creatorId = creatorId,
            creatorUsername = "paula",
            card =
                Card(
                    id = UUID.randomUUID(),
                    ownerId = creatorId,
                    name = "Mewtwo",
                    image = "mewtwo.png",
                    category = "Pokémon",
                    state = CardState.NEAR_MINT,
                ),
            minPrize = 50.0,
            startedAt = now.minus(1, ChronoUnit.DAYS),
            durationInDays = 1,
        )

    // Mockito devuelve null en any(); este helper evita el NPE de Kotlin con parámetros no nulos.
    @Suppress("UNCHECKED_CAST")
    private fun <T> anyObject(): T {
        ArgumentMatchers.any<T>()
        return null as T
    }

    @Test
    @DisplayName("Debe notificar la expiracion con la puja ganadora cuando hubo pujas")
    fun `should notify expired auctions with winning bid`() {
        val winningBid =
            Bid(auctionId = auctionId, bidderId = UUID.randomUUID(), bidderUsername = "carlos", amount = 75.0)
        `when`(auctionRepository.getJustExpired(now)).thenReturn(listOf(auction))
        `when`(bidRepository.findHighestBidByAuctionId(auctionId)).thenReturn(winningBid)

        useCase.execute(now)

        verify(auctionRepository, times(1)).getJustExpired(now)
        verify(notificationPort, times(1)).notifyAuctionExpired(auctionId, winningBid)
    }

    @Test
    @DisplayName("Debe notificar la expiracion sin ganador cuando nadie pujo")
    fun `should notify expired auctions without winner when there are no bids`() {
        `when`(auctionRepository.getJustExpired(now)).thenReturn(listOf(auction))
        `when`(bidRepository.findHighestBidByAuctionId(auctionId)).thenReturn(null)

        useCase.execute(now)

        verify(notificationPort, times(1)).notifyAuctionExpired(auctionId, null)
    }

    @Test
    @DisplayName("Debe enviar push al creador, al ganador y a favoritos (excluyendo al ganador)")
    fun `should push creator winner and favorites when there is a winning bid`() {
        val winnerId = UUID.randomUUID()
        val favUserId = UUID.randomUUID()
        val winningBid =
            Bid(auctionId = auctionId, bidderId = winnerId, bidderUsername = "carlos", amount = 75.0)
        `when`(auctionRepository.getJustExpired(now)).thenReturn(listOf(auction))
        `when`(bidRepository.findHighestBidByAuctionId(auctionId)).thenReturn(winningBid)
        `when`(favoriteAuctionRepository.findUserIdsByAuctionId(auctionId)).thenReturn(listOf(winnerId, favUserId))

        useCase.execute(now)

        verify(pushNotificationPort, times(1))
            .send(listOf(creatorId), PushMessages.ownAuctionEnded(auctionId, "Mewtwo", winningBid))
        verify(pushNotificationPort, times(1))
            .send(listOf(winnerId), PushMessages.auctionWon(auctionId, "Mewtwo", 75.0))
        verify(pushNotificationPort, times(1))
            .send(listOf(favUserId), PushMessages.favoriteEnded(auctionId, "Mewtwo"))
    }

    @Test
    @DisplayName("Solo debe avisar al creador (y favoritos vacíos) cuando nadie pujo")
    fun `should push only the creator when there are no bids`() {
        `when`(auctionRepository.getJustExpired(now)).thenReturn(listOf(auction))
        `when`(bidRepository.findHighestBidByAuctionId(auctionId)).thenReturn(null)
        `when`(favoriteAuctionRepository.findUserIdsByAuctionId(auctionId)).thenReturn(emptyList())

        useCase.execute(now)

        verify(pushNotificationPort, times(1))
            .send(listOf(creatorId), PushMessages.ownAuctionEnded(auctionId, "Mewtwo", null))
        verify(pushNotificationPort, times(1))
            .send(emptyList(), PushMessages.favoriteEnded(auctionId, "Mewtwo"))
        verify(pushNotificationPort, never())
            .send(anyObject(), ArgumentMatchers.argThat { it?.type?.name == "AUCTION_WON" } ?: anyObject())
    }
}
