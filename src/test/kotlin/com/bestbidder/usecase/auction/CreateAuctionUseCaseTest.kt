package com.bestbidder.usecase.auction

import com.bestbidder.domain.auction.Auction
import com.bestbidder.usecase.port.AuctionRepository
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

class CreateAuctionUseCaseTest {
    private val auctionRepository: AuctionRepository = mock(AuctionRepository::class.java)
    private val useCase = CreateAuctionUseCase(auctionRepository)
    private val now = Instant.now()

    @Test
    @DisplayName("Debe crear y guardar la subasta si no existe ninguna previa activa")
    fun `should create auction successfully when non exists`() {
        // Arrange
        val creatorId = UUID.randomUUID()
        val creatorUsername = "paula"
        val card = "Pikachu Illustrator"

        `when`(auctionRepository.existsByCreatorAndCard(creatorUsername, card)).thenReturn(false)
        `when`(auctionRepository.save(any())).thenAnswer { invocation -> invocation.getArgument(0) as Auction }

        // Act
        val result =
            useCase.create(
                creator_id = creatorId,
                creator_username = creatorUsername,
                card = card,
                collection = "Promo",
                minPrize = 500.0,
                startedAt = now,
                durationInDays = 3,
            )

        // Assert
        assertNotNull(result)
        assertEquals(creatorId, result.creator_id)
        assertEquals(creatorUsername, result.creator_username)
        assertEquals(card, result.card)

        verify(auctionRepository, times(1)).existsByCreatorAndCard(creatorUsername, card)
        verify(auctionRepository, times(1)).save(any())
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException si ya existe una subasta de esa carta y usuario")
    fun `should throw exception when duplicate auction exists`() {
        // Arrange
        val creatorId = UUID.randomUUID()
        val creatorUsername = "paula"
        val card = "Charizard 1st Edition"

        `when`(auctionRepository.existsByCreatorAndCard(creatorUsername, card)).thenReturn(true)

        // Act & Assert
        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                useCase.create(
                    creator_id = creatorId,
                    creator_username = creatorUsername,
                    card = card,
                    collection = "Base Set",
                    minPrize = 150.0,
                    startedAt = now,
                    durationInDays = 2,
                )
            }

        assertEquals(
            "Ya existe una subasta activa o próxima de la carta '$card' para el usuario '$creatorUsername'",
            exception.message,
        )

        verify(auctionRepository, times(1)).existsByCreatorAndCard(creatorUsername, card)
        verify(auctionRepository, never()).save(any())
    }
}
