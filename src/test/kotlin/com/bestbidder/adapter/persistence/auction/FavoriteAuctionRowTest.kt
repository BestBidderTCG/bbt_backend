package com.bestbidder.adapter.persistence.auction

import com.bestbidder.domain.auction.AuctionStatus
import com.bestbidder.domain.auction.NotificationMode
import com.bestbidder.domain.card.CardState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

class FavoriteAuctionRowTest {
    private val now = Instant.now()

    private fun buildRow(
        startedAt: Instant = now.minus(1, ChronoUnit.DAYS),
        endsAt: Instant = now.plus(2, ChronoUnit.DAYS),
        notificationMode: NotificationMode = NotificationMode.BASIC,
    ) = FavoriteAuctionRow(
        id = UUID.randomUUID(),
        cardName = "Gengar",
        cardImage = "gengar.png",
        currentBid = 120.0,
        startedAt = startedAt,
        endsAt = endsAt,
        cardState = CardState.GOOD,
        cardCategory = "Pokémon",
        cardCollection = "Base Set",
        cardNumber = 5,
        notificationMode = notificationMode,
    )

    @Test
    @DisplayName("Copia los datos de la subasta y conserva el modo de notificación")
    fun `should map auction data and keep notification mode`() {
        val row = buildRow(notificationMode = NotificationMode.ALL)

        val item = row.toFavoriteItem(now)

        assertEquals(row.id, item.auction.id)
        assertEquals("Gengar", item.auction.cardName)
        assertEquals("gengar.png", item.auction.cardImage)
        assertEquals(120.0, item.auction.currentBid)
        assertEquals(CardState.GOOD, item.auction.cardState)
        assertEquals("Pokémon", item.auction.cardCategory)
        assertEquals("Base Set", item.auction.cardCollection)
        assertEquals(5, item.auction.cardNumber)
        assertEquals(NotificationMode.ALL, item.notificationMode)
    }

    @Test
    @DisplayName("Cada modo de notificación se propaga sin modificarse")
    fun `should propagate every notification mode`() {
        NotificationMode.entries.forEach { mode ->
            assertEquals(mode, buildRow(notificationMode = mode).toFavoriteItem(now).notificationMode)
        }
    }

    @Test
    @DisplayName("Una subasta que ya ha empezado y no ha terminado está IN_PROGRESS")
    fun `should be in progress when started and not ended`() {
        val item = buildRow().toFavoriteItem(now)

        assertEquals(AuctionStatus.IN_PROGRESS, item.auction.status)
        assertEquals(2, item.auction.remainingDays)
    }

    @Test
    @DisplayName("Una subasta que aún no ha empezado está UPCOMING y cuenta los días desde su inicio")
    fun `should be upcoming when not started yet`() {
        val row =
            buildRow(
                startedAt = now.plus(1, ChronoUnit.DAYS),
                endsAt = now.plus(4, ChronoUnit.DAYS),
            )

        val item = row.toFavoriteItem(now)

        assertEquals(AuctionStatus.UPCOMING, item.auction.status)
        assertEquals(3, item.auction.remainingDays)
    }

    @Test
    @DisplayName("Una subasta terminada está FINISHED y tiene 0 días restantes")
    fun `should be finished when already ended`() {
        val row =
            buildRow(
                startedAt = now.minus(5, ChronoUnit.DAYS),
                endsAt = now.minus(1, ChronoUnit.HOURS),
            )

        val item = row.toFavoriteItem(now)

        assertEquals(AuctionStatus.FINISHED, item.auction.status)
        assertEquals(0, item.auction.remainingDays)
    }

    @Test
    @DisplayName("Los campos opcionales de la carta pueden ser null")
    fun `should allow null collection and number`() {
        val row = buildRow().copy(cardCollection = null, cardNumber = null)

        val item = row.toFavoriteItem(now)

        assertNull(item.auction.cardCollection)
        assertNull(item.auction.cardNumber)
    }
}
