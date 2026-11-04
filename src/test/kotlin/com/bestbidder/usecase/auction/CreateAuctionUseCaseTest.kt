package com.bestbidder.usecase.auction

import com.bestbidder.domain.auction.Auction
import com.bestbidder.usecase.port.AuctionRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
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
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

class CreateAuctionUseCaseTest {
    private val auctionRepository: AuctionRepository = mock(AuctionRepository::class.java)
    private val useCase = CreateAuctionUseCase(auctionRepository)

    /**
     * Genera una fecha futura válida alineada al próximo bloque de 15 minutos en UTC.
     */
    private fun getValidFutureStart(): Instant {
        val nowUtc = ZonedDateTime.now(ZoneOffset.UTC)
        val currentMinute = nowUtc.minute
        val minutesToAdd = 15 - (currentMinute % 15)

        return nowUtc.plusMinutes(minutesToAdd.toLong())
            .truncatedTo(ChronoUnit.MINUTES)
            .toInstant()
    }

    @Test
    @DisplayName("Debe crear y guardar la subasta con descripción si cumple todas las validaciones")
    fun `should create auction successfully with description`() {
        // Arrange
        val creatorId = UUID.randomUUID()
        val creatorUsername = "paula"
        val card = "Pikachu Illustrator"
        val description = "Carta en perfecto estado PSA 10"
        val validStart = getValidFutureStart()

        `when`(auctionRepository.existsByCreatorAndCard(creatorUsername, card)).thenReturn(false)
        `when`(auctionRepository.save(any())).thenAnswer { invocation -> invocation.getArgument(0) as Auction }

        // Act
        val result =
            useCase.create(
                creator_id = creatorId,
                creator_username = creatorUsername,
                card = card,
                collection = "Promo",
                description = description,
                minPrize = 500.0,
                startedAt = validStart,
                durationInDays = 3,
            )

        // Assert
        assertNotNull(result)
        assertEquals(creatorId, result.creator_id)
        assertEquals(creatorUsername, result.creator_username)
        assertEquals(card, result.card)
        assertEquals(description, result.description)

        verify(auctionRepository, times(1)).existsByCreatorAndCard(creatorUsername, card)
        verify(auctionRepository, times(1)).save(any())
    }

    @Test
    @DisplayName("Debe crear la subasta correctamente cuando la descripción es nula")
    fun `should create auction successfully when description is null`() {
        // Arrange
        val creatorId = UUID.randomUUID()
        val creatorUsername = "paula"
        val card = "Lugui"
        val validStart = getValidFutureStart()

        `when`(auctionRepository.existsByCreatorAndCard(creatorUsername, card)).thenReturn(false)
        `when`(auctionRepository.save(any())).thenAnswer { invocation -> invocation.getArgument(0) as Auction }

        // Act
        val result =
            useCase.create(
                creator_id = creatorId,
                creator_username = creatorUsername,
                card = card,
                collection = "Neo Genesis",
                description = null,
                minPrize = 300.0,
                startedAt = validStart,
                durationInDays = 2,
            )

        // Assert
        assertNotNull(result)
        assertNull(result.description)

        verify(auctionRepository, times(1)).save(any())
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException si ya existe una subasta de esa carta y usuario")
    fun `should throw exception when duplicate auction exists`() {
        // Arrange
        val creatorId = UUID.randomUUID()
        val creatorUsername = "paula"
        val card = "Charizard 1st Edition"
        val validStart = getValidFutureStart()

        `when`(auctionRepository.existsByCreatorAndCard(creatorUsername, card)).thenReturn(true)

        // Act & Assert
        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                useCase.create(
                    creator_id = creatorId,
                    creator_username = creatorUsername,
                    card = card,
                    collection = "Base Set",
                    description = "Primera edición",
                    minPrize = 150.0,
                    startedAt = validStart,
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

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException si la fecha de inicio es en el pasado")
    fun `should throw exception when startedAt is in the past`() {
        // Arrange
        val pastStart = Instant.now().minus(10, ChronoUnit.MINUTES).truncatedTo(ChronoUnit.MINUTES)

        // Act & Assert
        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                useCase.create(
                    creator_id = UUID.randomUUID(),
                    creator_username = "paula",
                    card = "Blastoise",
                    collection = "Base Set",
                    description = null,
                    minPrize = 100.0,
                    startedAt = pastStart,
                    durationInDays = 1,
                )
            }

        assertEquals(
            "La fecha de inicio de la subasta no puede ser anterior al instante actual",
            exception.message,
        )
        verify(auctionRepository, never()).save(any())
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException si los minutos no estan alineados a bloques de 15")
    fun `should throw exception when startedAt minute is not aligned to 15m intervals`() {
        // Arrange: minuto 10 (inválido)
        val unalignedStart =
            ZonedDateTime.now(ZoneOffset.UTC)
                .plusHours(1)
                .withMinute(10)
                .withSecond(0)
                .withNano(0)
                .toInstant()

        // Act & Assert
        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                useCase.create(
                    creator_id = UUID.randomUUID(),
                    creator_username = "paula",
                    card = "Venusaur",
                    collection = "Base Set",
                    description = null,
                    minPrize = 80.0,
                    startedAt = unalignedStart,
                    durationInDays = 1,
                )
            }

        assertEquals(
            "La hora de activación debe estar alineada a bloques de 15 minutos exactos (ej. :00, :15, :30, :45)",
            exception.message,
        )
        verify(auctionRepository, never()).save(any())
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException si la duracion es menor a 1 dia")
    fun `should throw exception when durationInDays is less than 1`() {
        // Arrange
        val validStart = getValidFutureStart()

        // Act & Assert
        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                useCase.create(
                    creator_id = UUID.randomUUID(),
                    creator_username = "paula",
                    card = "Mewtwo",
                    collection = "Promo",
                    description = null,
                    minPrize = 90.0,
                    startedAt = validStart,
                    durationInDays = 0,
                )
            }

        assertEquals(
            "La duración de la subasta debe ser de al menos 1 día",
            exception.message,
        )
        verify(auctionRepository, never()).save(any())
    }
}
