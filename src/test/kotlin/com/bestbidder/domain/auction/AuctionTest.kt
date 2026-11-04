package com.bestbidder.domain.auction

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

class AuctionTest {
    private val now = Instant.now()
    private val userId = UUID.randomUUID()

    @Test
    @DisplayName("Debe calcular endsAt correctamente basándose en startedAt y durationInDays")
    fun `should calculate endsAt correctly`() {
        val startedAt = now
        val durationInDays = 3

        val auction =
            Auction(
                creator_id = userId,
                creator_username = "paula",
                card = "Charizard",
                collection = "Base Set",
                min_prize = 100.0,
                startedAt = startedAt,
                durationInDays = durationInDays,
            )

        val expectedEndsAt = startedAt.plus(3, ChronoUnit.DAYS)
        assertEquals(expectedEndsAt, auction.endsAt)
    }

    @Test
    @DisplayName("Debe lanzar excepción si durationInDays es menor a 1 o mayor a 7")
    fun `should throw exception when durationInDays is out of range`() {
        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                Auction(
                    creator_id = userId,
                    creator_username = "paula",
                    card = "Charizard",
                    collection = "Base Set",
                    min_prize = 100.0,
                    startedAt = now,
                    durationInDays = 0,
                )
            }

        assertEquals("La duración de la subasta debe estar entre 1 y 7 días", exception.message)
    }

    @Test
    @DisplayName("Debe retornar CANCELLED cuando isCancelled es true")
    fun `should return CANCELLED when isCancelled is true`() {
        val auction =
            Auction(
                creator_id = userId,
                creator_username = "paula",
                card = "Charizard",
                collection = "Base Set",
                min_prize = 100.0,
                startedAt = now.minus(1, ChronoUnit.DAYS),
                durationInDays = 3,
                isCancelled = true,
            )

        assertEquals(AuctionStatus.CANCELLED, auction.getStatusAt(now))
    }

    @Test
    @DisplayName("Debe retornar UPCOMING cuando la fecha de inicio es futura")
    fun `should return UPCOMING when startedAt is in the future`() {
        val futureStart = now.plus(1, ChronoUnit.HOURS)

        val auction =
            Auction(
                creator_id = userId,
                creator_username = "paula",
                card = "Charizard",
                collection = "Base Set",
                min_prize = 100.0,
                startedAt = futureStart,
                durationInDays = 2,
            )

        assertEquals(AuctionStatus.UPCOMING, auction.getStatusAt(now))
    }

    @Test
    @DisplayName("Debe retornar IN_PROGRESS cuando la hora actual está entre startedAt y endsAt")
    fun `should return IN_PROGRESS when current time is between startedAt and endsAt`() {
        val pastStart = now.minus(1, ChronoUnit.DAYS)

        val auction =
            Auction(
                creator_id = userId,
                creator_username = "paula",
                card = "Charizard",
                collection = "Base Set",
                min_prize = 100.0,
                startedAt = pastStart,
                durationInDays = 3,
            )

        assertEquals(AuctionStatus.IN_PROGRESS, auction.getStatusAt(now))
    }

    @Test
    @DisplayName("Debe retornar FINISHED cuando la hora actual ha superado endsAt")
    fun `should return FINISHED when endsAt has passed`() {
        val pastStart = now.minus(5, ChronoUnit.DAYS)

        val auction =
            Auction(
                creator_id = userId,
                creator_username = "paula",
                card = "Charizard",
                collection = "Base Set",
                min_prize = 100.0,
                startedAt = pastStart,
                durationInDays = 2,
            )

        assertEquals(AuctionStatus.FINISHED, auction.getStatusAt(now))
    }
}
