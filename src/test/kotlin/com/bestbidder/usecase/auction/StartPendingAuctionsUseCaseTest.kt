package com.bestbidder.usecase.auction

import com.bestbidder.domain.auction.Auction
import com.bestbidder.domain.auction.NotificationMode
import com.bestbidder.domain.card.Card
import com.bestbidder.domain.card.CardState
import com.bestbidder.domain.notification.Audience
import com.bestbidder.domain.notification.PushMessages
import com.bestbidder.domain.notification.SseEvents
import com.bestbidder.usecase.port.AuctionRepository
import com.bestbidder.usecase.port.FavoriteAuctionRepository
import com.bestbidder.usecase.port.PushNotificationPort
import com.bestbidder.usecase.port.RealtimeEventPort
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

class StartPendingAuctionsUseCaseTest {
    private val auctionRepository: AuctionRepository = mock(AuctionRepository::class.java)
    private val realtimeEventPort: RealtimeEventPort = mock(RealtimeEventPort::class.java)
    private val favoriteAuctionRepository: FavoriteAuctionRepository = mock(FavoriteAuctionRepository::class.java)
    private val pushNotificationPort: PushNotificationPort = mock(PushNotificationPort::class.java)
    private val useCase =
        StartPendingAuctionsUseCase(
            auctionRepository,
            realtimeEventPort,
            favoriteAuctionRepository,
            pushNotificationPort,
        )

    @Test
    @DisplayName("Debe notificar activacion por SSE y enviar push al creador y a favoritos")
    fun `should notify started auctions`() {
        val now = Instant.now().truncatedTo(ChronoUnit.MINUTES)
        val auctionId = UUID.randomUUID()
        val creatorId = UUID.randomUUID()
        val favUserId = UUID.randomUUID()

        val auction =
            Auction(
                id = auctionId,
                creatorId = creatorId,
                creatorUsername = "paula",
                card =
                    Card(
                        id = UUID.randomUUID(),
                        ownerId = creatorId,
                        name = "Charizard",
                        image = "charizard.png",
                        category = "Pokémon",
                        state = CardState.POOR,
                    ),
                minPrize = 100.0,
                startedAt = now,
                durationInDays = 1,
            )

        `when`(auctionRepository.getJustStarted(now)).thenReturn(listOf(auction))
        `when`(favoriteAuctionRepository.findUserIdsByAuctionId(auctionId, NotificationMode.LIFECYCLE))
            .thenReturn(listOf(favUserId))

        useCase.execute(now)

        verify(auctionRepository, times(1)).getJustStarted(now)
        verify(realtimeEventPort, times(1)).publish(SseEvents.auctionStarted(auctionId), Audience.Everyone)
        verify(pushNotificationPort, times(1)).send(
            listOf(creatorId),
            PushMessages.ownAuctionStarted(auctionId, "Charizard"),
        )
        verify(pushNotificationPort, times(1)).send(
            listOf(favUserId),
            PushMessages.favoriteStarted(auctionId, "Charizard"),
        )
    }
}
