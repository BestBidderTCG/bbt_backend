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

class CloseExpiredAuctionsUseCaseTest {
    private val auctionRepository: AuctionRepository = mock(AuctionRepository::class.java)
    private val notificationPort: AuctionNotificationPort = mock(AuctionNotificationPort::class.java)
    private val useCase = CloseExpiredAuctionsUseCase(auctionRepository, notificationPort)

    @Test
    @DisplayName("Debe notificar expiracion cuando se alcanza el momento de finalizacion")
    fun `should notify expired auctions`() {
        val now = Instant.now().truncatedTo(ChronoUnit.MINUTES)
        val auctionId = UUID.randomUUID()
        val auction =
            Auction(
                id = auctionId,
                creator_id = UUID.randomUUID(),
                creator_username = "paula",
                card = "Mewtwo",
                category = "Pokémon",
                collection = "Promo",
                min_prize = 50.0,
                startedAt = now.minus(1, ChronoUnit.DAYS),
                durationInDays = 1,
            )

        `when`(auctionRepository.getJustExpired(now)).thenReturn(listOf(auction))

        useCase.execute(now)

        verify(auctionRepository, times(1)).getJustExpired(now)
        verify(notificationPort, times(1)).notifyAuctionExpired(auctionId)
    }
}
