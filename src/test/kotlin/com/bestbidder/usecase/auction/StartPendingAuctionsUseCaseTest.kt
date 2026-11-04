package com.bestbidder.usecase.auction

import com.bestbidder.domain.auction.Auction
import com.bestbidder.usecase.port.AuctionNotificationPort
import com.bestbidder.usecase.port.AuctionRepository
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
    private val notificationPort: AuctionNotificationPort = mock(AuctionNotificationPort::class.java)
    private val useCase = StartPendingAuctionsUseCase(auctionRepository, notificationPort)

    @Test
    @DisplayName("Debe notificar activacion cuando existen subastas pendientes de iniciar")
    fun `should notify started auctions`() {
        val now = Instant.now().truncatedTo(ChronoUnit.MINUTES)
        val auctionId = UUID.randomUUID()
        val auction =
            Auction(
                id = auctionId,
                creator_id = UUID.randomUUID(),
                creator_username = "paula",
                card = "Charizard",
                category = "Pokémon",
                collection = "Base Set",
                min_prize = 100.0,
                startedAt = now,
                durationInDays = 1,
            )

        `when`(auctionRepository.getJustStarted(now)).thenReturn(listOf(auction))

        useCase.execute(now)

        verify(auctionRepository, times(1)).getJustStarted(now)
        verify(notificationPort, times(1)).notifyAuctionStarted(auctionId)
    }
}
