package com.bestbidder.usecase.auction

import com.bestbidder.domain.auction.AuctionSummary
import com.bestbidder.domain.card.CardState
import com.bestbidder.usecase.port.AuctionRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.util.UUID

class GetAuctionUseCaseTest {
    private val auctionRepository: AuctionRepository = mock(AuctionRepository::class.java)
    private val useCase = GetAuctionUseCase(auctionRepository)

    private fun buildSampleSummary(
        cardName: String = "Gengar",
        currentBid: Double = 80.0,
        remainingDays: Int = 3,
    ) = AuctionSummary(
        id = UUID.randomUUID(),
        cardName = cardName,
        cardImage = "${cardName.lowercase()}.png",
        currentBid = currentBid,
        remainingDays = remainingDays,
        cardState = CardState.POOR,
        cardCategory = "Pokémon",
        cardCollection = null,
    )

    @Test
    @DisplayName("Debe obtener el resumen de todas las subastas")
    fun `should get all auction summaries`() {
        val summaries =
            listOf(
                buildSampleSummary(cardName = "Gengar"),
                buildSampleSummary(cardName = "Charizard"),
            )
        `when`(auctionRepository.getAllSummaries()).thenReturn(summaries)

        val result = useCase.getAll()

        assertEquals(2, result.size)
        assertEquals("Gengar", result[0].cardName)
        assertEquals("Charizard", result[1].cardName)
        verify(auctionRepository, times(1)).getAllSummaries()
    }
}
