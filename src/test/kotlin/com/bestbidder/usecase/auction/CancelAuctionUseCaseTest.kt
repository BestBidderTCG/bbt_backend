package com.bestbidder.usecase.auction

import com.bestbidder.domain.auction.Auction
import com.bestbidder.usecase.port.AuctionNotificationPort
import com.bestbidder.usecase.port.AuctionRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.argThat
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.time.Instant
import java.util.UUID

class CancelAuctionUseCaseTest {
    private val auctionRepository: AuctionRepository = mock(AuctionRepository::class.java)
    private val notificationPort: AuctionNotificationPort = mock(AuctionNotificationPort::class.java)
    private val useCase = CancelAuctionUseCase(auctionRepository, notificationPort)

    @Test
    @DisplayName("Debe cancelar la subasta correctamente si la realiza el creador")
    fun `should cancel auction when requested by creator`() {
        val auctionId = UUID.randomUUID()
        val creatorId = UUID.randomUUID()
        val auction =
            Auction(
                id = auctionId,
                creator_id = creatorId,
                creator_username = "paula",
                card = "Pikachu Gold",
                category = "Pokémon",
                collection = "Promo",
                min_prize = 200.0,
                startedAt = Instant.now(),
                durationInDays = 2,
                isCancelled = false,
            )

        `when`(auctionRepository.getById(auctionId)).thenReturn(auction)

        useCase.cancel(auctionId, creatorId)

        verify(auctionRepository, times(1)).save(argThat { it.id == auctionId && it.isCancelled })
        verify(notificationPort, times(1)).notifyAuctionCancelled(auctionId)
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException si intenta cancelar un usuario distinto al creador")
    fun `should throw exception when requested by non creator`() {
        val auctionId = UUID.randomUUID()
        val creatorId = UUID.randomUUID()
        val impostorId = UUID.randomUUID()
        val auction =
            Auction(
                id = auctionId,
                creator_id = creatorId,
                creator_username = "paula",
                card = "Pikachu Gold",
                category = "Pokémon",
                collection = "Promo",
                min_prize = 200.0,
                startedAt = Instant.now(),
                durationInDays = 2,
            )

        `when`(auctionRepository.getById(auctionId)).thenReturn(auction)

        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                useCase.cancel(auctionId, impostorId)
            }

        assertEquals("No tienes permiso para cancelar esta subasta", exception.message)
        verify(auctionRepository, never()).save(argThat { it.isCancelled })
        verify(notificationPort, never()).notifyAuctionCancelled(auctionId)
    }

    @Test
    @DisplayName("Debe lanzar IllegalStateException si la subasta ya habia sido cancelada")
    fun `should throw exception when auction is already cancelled`() {
        val auctionId = UUID.randomUUID()
        val creatorId = UUID.randomUUID()
        val cancelledAuction =
            Auction(
                id = auctionId,
                creator_id = creatorId,
                creator_username = "paula",
                card = "Pikachu Gold",
                category = "Pokémon",
                collection = "Promo",
                min_prize = 200.0,
                startedAt = Instant.now(),
                durationInDays = 2,
                isCancelled = true,
            )

        `when`(auctionRepository.getById(auctionId)).thenReturn(cancelledAuction)

        val exception =
            assertThrows(IllegalStateException::class.java) {
                useCase.cancel(auctionId, creatorId)
            }

        assertEquals("La subasta ya se encuentra cancelada", exception.message)
        verify(notificationPort, never()).notifyAuctionCancelled(auctionId)
    }
}
