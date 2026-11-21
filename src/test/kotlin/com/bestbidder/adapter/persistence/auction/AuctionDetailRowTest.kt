package com.bestbidder.adapter.persistence.auction

import com.bestbidder.domain.auction.AuctionStatus
import com.bestbidder.domain.card.CardState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

class AuctionDetailRowTest {
    private val now = Instant.now()
    private val creatorId = UUID.randomUUID()

    private fun buildRow() =
        AuctionDetailRow(
            id = UUID.randomUUID(),
            cardName = "Gengar",
            cardImage = "gengar.png",
            cardCollection = "Base Set",
            cardNumber = 5,
            cardState = CardState.POOR,
            minPrize = 80.0,
            lastBid = 100.0,
            bidCount = 2,
            startedAt = now.minus(1, ChronoUnit.DAYS),
            endsAt = now.plus(1, ChronoUnit.DAYS),
            description = "desc",
            creatorId = creatorId,
            creatorUsername = "paula",
            creatorAvatarNum = 3,
            isFavorite = true,
        )

    @Test
    @DisplayName("Si quien consulta es el propietario, creador e isFavorite son null")
    fun `should hide creator and favorite when viewer is owner`() {
        val detail = buildRow().toDetail(now, creatorId)

        assertNull(detail.creator)
        assertNull(detail.isFavorite)
        assertEquals("Gengar", detail.cardName)
    }

    @Test
    @DisplayName("Si quien consulta es otro usuario, devuelve creador e isFavorite")
    fun `should return creator and favorite for another viewer`() {
        val detail = buildRow().copy(isFavorite = true).toDetail(now, UUID.randomUUID())

        assertNotNull(detail.creator)
        assertEquals(creatorId, detail.creator?.id)
        assertEquals("paula", detail.creator?.username)
        assertEquals(3, detail.creator?.avatarNum)
        assertEquals(true, detail.isFavorite)
    }

    @Test
    @DisplayName("Si otro usuario no la tiene en favoritos, isFavorite es false")
    fun `should return false when viewer has not favorited`() {
        val detail = buildRow().copy(isFavorite = false).toDetail(now, UUID.randomUUID())

        assertEquals(false, detail.isFavorite)
    }

    @Test
    @DisplayName("Sin autenticar devuelve creador pero isFavorite null")
    fun `should return creator but null favorite when anonymous`() {
        val detail = buildRow().copy(isFavorite = false).toDetail(now, null)

        assertNotNull(detail.creator)
        assertNull(detail.isFavorite)
    }

    @Test
    @DisplayName("Calcula el estado según las fechas")
    fun `should compute status from dates`() {
        val upcoming = buildRow().copy(startedAt = now.plusSeconds(60), endsAt = now.plusSeconds(3600))
        val inProgress = buildRow()
        val finished = buildRow().copy(startedAt = now.minusSeconds(3600), endsAt = now.minusSeconds(60))

        assertEquals(AuctionStatus.UPCOMING, upcoming.toDetail(now, null).status)
        assertEquals(AuctionStatus.IN_PROGRESS, inProgress.toDetail(now, null).status)
        assertEquals(AuctionStatus.FINISHED, finished.toDetail(now, null).status)
    }

    @Test
    @DisplayName("Una subasta que termina justo ahora ya está finalizada")
    fun `should be finished when endsAt equals now`() {
        val row = buildRow().copy(startedAt = now.minusSeconds(3600), endsAt = now)

        assertEquals(AuctionStatus.FINISHED, row.toDetail(now, null).status)
    }

    @Test
    @DisplayName("El incremento se calcula sobre la última puja si existe")
    fun `should compute increment from last bid`() {
        val detail = buildRow().copy(minPrize = 10.0, lastBid = 120.0).toDetail(now, null)

        assertEquals(5.0, detail.increment)
    }

    @Test
    @DisplayName("Sin pujas el incremento se calcula sobre el precio mínimo")
    fun `should compute increment from min prize when no bids`() {
        val detail = buildRow().copy(minPrize = 3.0, lastBid = null).toDetail(now, null)

        assertEquals(0.25, detail.increment)
    }
}
