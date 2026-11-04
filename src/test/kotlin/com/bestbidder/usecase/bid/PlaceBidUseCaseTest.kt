package com.bestbidder.usecase.bid

import com.bestbidder.domain.auction.Auction
import com.bestbidder.domain.bid.Bid
import com.bestbidder.usecase.port.AuctionNotificationPort
import com.bestbidder.usecase.port.AuctionRepository
import com.bestbidder.usecase.port.BidRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.time.Instant
import java.util.UUID

class PlaceBidUseCaseTest {
    private val auctionRepository: AuctionRepository = mock(AuctionRepository::class.java)
    private val bidRepository: BidRepository = mock(BidRepository::class.java)
    private val notificationPort: AuctionNotificationPort = mock(AuctionNotificationPort::class.java)

    private val useCase = PlaceBidUseCase(auctionRepository, bidRepository, notificationPort)

    @Test
    @DisplayName("Debe registrar la puja si cumple con el incremento mínimo obligatorio según el rango")
    fun `should place bid successfully when satisfying minimum bid increment`() {
        val auctionId = UUID.randomUUID()
        val creatorId = UUID.randomUUID()
        val previousBidderId = UUID.randomUUID()
        val newBidderId = UUID.randomUUID()

        val activeAuction =
            Auction(
                id = auctionId,
                creator_id = creatorId,
                creator_username = "paula",
                card = "Charizard First Edition",
                collection = "Base Set",
                description = "Perfecto estado",
                min_prize = 100.0,
                startedAt = Instant.now().minusSeconds(3600),
                durationInDays = 1,
            )

        // Puja previa de 150.0€ -> Al estar entre 100€ y 250€, el incremento mínimo es 5.0€
        val previousBid =
            Bid(
                auctionId = auctionId,
                bidderId = previousBidderId,
                bidderUsername = "pujador1",
                amount = 150.0,
            )

        `when`(auctionRepository.getById(auctionId)).thenReturn(activeAuction)
        `when`(bidRepository.findHighestBidByAuctionId(auctionId)).thenReturn(previousBid)
        `when`(bidRepository.save(any(Bid::class.java))).thenAnswer { it.arguments[0] as Bid }

        // Pujamos 155.0€ (cumple justo el incremento mínimo de 5.0€)
        val result =
            useCase.execute(
                auctionId = auctionId,
                bidderId = newBidderId,
                bidderUsername = "pujador2",
                amount = 155.0,
            )

        assertNotNull(result)
        assertEquals(155.0, result.amount)
        assertEquals(newBidderId, result.bidderId)

        verify(bidRepository, times(1)).save(any(Bid::class.java))
        verify(notificationPort, times(1)).notifyNewBidPlaced(
            auctionId = auctionId,
            newAmount = 155.0,
            bidderUsername = "pujador2",
        )
    }

    @Test
    @DisplayName("Debe registrar la primera puja si es mayor o igual al min_prize de la subasta")
    fun `should place first bid successfully when equal or higher than min_prize`() {
        val auctionId = UUID.randomUUID()
        val creatorId = UUID.randomUUID()
        val newBidderId = UUID.randomUUID()

        val activeAuction =
            Auction(
                id = auctionId,
                creator_id = creatorId,
                creator_username = "paula",
                card = "Pikachu Illustrator",
                collection = "Promo",
                description = null,
                min_prize = 50.0,
                startedAt = Instant.now().minusSeconds(3600),
                durationInDays = 1,
            )

        `when`(auctionRepository.getById(auctionId)).thenReturn(activeAuction)
        `when`(bidRepository.findHighestBidByAuctionId(auctionId)).thenReturn(null)
        `when`(bidRepository.save(any(Bid::class.java))).thenAnswer { it.arguments[0] as Bid }

        val result =
            useCase.execute(
                auctionId = auctionId,
                bidderId = newBidderId,
                bidderUsername = "pujador1",
                amount = 50.0,
            )

        assertNotNull(result)
        assertEquals(50.0, result.amount)
        verify(bidRepository, times(1)).save(any(Bid::class.java))
    }

    @Test
    @DisplayName("Debe lanzar excepcion si la puja no alcanza el incremento mínimo requerido")
    fun `should throw exception when bid is below the minimum required increment`() {
        val auctionId = UUID.randomUUID()
        val creatorId = UUID.randomUUID()
        val bidderId = UUID.randomUUID()

        val activeAuction =
            Auction(
                id = auctionId,
                creator_id = creatorId,
                creator_username = "paula",
                card = "Charizard",
                collection = "Base Set",
                description = null,
                min_prize = 100.0,
                startedAt = Instant.now().minusSeconds(3600),
                durationInDays = 1,
            )

        // Puja actual en 150.0€ -> El mínimo requerido para la siguiente es 155.0€
        val existingHighestBid =
            Bid(
                auctionId = auctionId,
                bidderId = UUID.randomUUID(),
                bidderUsername = "pujador1",
                amount = 150.0,
            )

        `when`(auctionRepository.getById(auctionId)).thenReturn(activeAuction)
        `when`(bidRepository.findHighestBidByAuctionId(auctionId)).thenReturn(existingHighestBid)

        // Intentamos pujar 152.0€ (suficiente sobre 150.0€ en el modelo antiguo, pero insuficiente ahora)
        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                useCase.execute(
                    auctionId = auctionId,
                    bidderId = bidderId,
                    bidderUsername = "pujador2",
                    amount = 152.0,
                )
            }

        assertEquals("El importe mínimo para pujar debe ser de 155.0 €", exception.message)
        verify(bidRepository, never()).save(any(Bid::class.java))
    }

    @Test
    @DisplayName("Debe lanzar excepcion si el creador intenta pujar en su propia subasta")
    fun `should throw exception when creator tries to bid`() {
        val auctionId = UUID.randomUUID()
        val creatorId = UUID.randomUUID()

        val activeAuction =
            Auction(
                id = auctionId,
                creator_id = creatorId,
                creator_username = "paula",
                card = "Charizard",
                collection = "Base Set",
                description = null,
                min_prize = 100.0,
                startedAt = Instant.now().minusSeconds(3600),
                durationInDays = 1,
            )

        `when`(auctionRepository.getById(auctionId)).thenReturn(activeAuction)

        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                useCase.execute(
                    auctionId = auctionId,
                    bidderId = creatorId,
                    bidderUsername = "paula",
                    amount = 150.0,
                )
            }

        assertEquals("El creador de la subasta no puede pujar en ella", exception.message)
        verify(bidRepository, never()).save(any(Bid::class.java))
    }

    @Test
    @DisplayName("Debe lanzar excepcion si la subasta no ha comenzado (UPCOMING)")
    fun `should throw exception when auction is not in progress`() {
        val auctionId = UUID.randomUUID()
        val upcomingAuction =
            Auction(
                id = auctionId,
                creator_id = UUID.randomUUID(),
                creator_username = "paula",
                card = "Charizard",
                collection = "Base Set",
                description = null,
                min_prize = 100.0,
                startedAt = Instant.now().plusSeconds(3600),
                durationInDays = 1,
            )

        `when`(auctionRepository.getById(auctionId)).thenReturn(upcomingAuction)

        val exception =
            assertThrows(IllegalStateException::class.java) {
                useCase.execute(
                    auctionId = auctionId,
                    bidderId = UUID.randomUUID(),
                    bidderUsername = "pujador",
                    amount = 150.0,
                )
            }

        assertEquals("Solo se puede pujar en subastas en curso (IN_PROGRESS)", exception.message)
        verify(bidRepository, never()).save(any(Bid::class.java))
    }

    @Test
    @DisplayName("Debe lanzar excepcion si el pujador actual intenta sobrepujar su propia puja mas alta")
    fun `should throw exception when highest bidder tries to bid again`() {
        val auctionId = UUID.randomUUID()
        val creatorId = UUID.randomUUID()
        val bidderId = UUID.randomUUID()

        val activeAuction =
            Auction(
                id = auctionId,
                creator_id = creatorId,
                creator_username = "paula",
                card = "Charizard",
                collection = "Base Set",
                description = null,
                min_prize = 100.0,
                startedAt = Instant.now().minusSeconds(3600),
                durationInDays = 1,
            )

        val existingHighestBid =
            Bid(
                auctionId = auctionId,
                bidderId = bidderId,
                bidderUsername = "pujador1",
                amount = 150.0,
            )

        `when`(auctionRepository.getById(auctionId)).thenReturn(activeAuction)
        `when`(bidRepository.findHighestBidByAuctionId(auctionId)).thenReturn(existingHighestBid)

        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                useCase.execute(
                    auctionId = auctionId,
                    bidderId = bidderId,
                    bidderUsername = "pujador1",
                    amount = 160.0,
                )
            }

        assertEquals("No puedes pujar si ya tienes la puja más alta actual", exception.message)
        verify(bidRepository, never()).save(any(Bid::class.java))
    }
}
