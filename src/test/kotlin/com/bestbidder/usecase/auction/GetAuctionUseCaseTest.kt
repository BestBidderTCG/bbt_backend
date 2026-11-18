package com.bestbidder.usecase.auction

import com.bestbidder.domain.auction.Auction
import com.bestbidder.domain.auction.AuctionSummary
import com.bestbidder.domain.card.Card
import com.bestbidder.domain.card.CardState
import com.bestbidder.usecase.port.AuctionRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.time.Instant
import java.util.UUID

class GetAuctionUseCaseTest {
    private val auctionRepository: AuctionRepository = mock(AuctionRepository::class.java)
    private val useCase = GetAuctionUseCase(auctionRepository)

    private fun buildSampleAuction(
        id: UUID = UUID.randomUUID(),
        cardName: String = "Gengar",
    ): Auction {
        val creatorId = UUID.randomUUID()
        return Auction(
            id = id,
            creatorId = creatorId,
            creatorUsername = "paula",
            card =
                Card(
                    id = UUID.randomUUID(),
                    ownerId = creatorId,
                    name = cardName,
                    image = "${cardName.lowercase()}.png",
                    category = "Pokémon",
                    state = CardState.POOR,
                ),
            minPrize = 80.0,
            startedAt = Instant.now(),
            durationInDays = 3,
        )
    }

    private fun buildSampleSummary(
        cardName: String = "Gengar",
        currentBid: Double = 80.0,
        remainingDays: Int = 3,
        cardNumber: Int? = null,
    ) = AuctionSummary(
        id = UUID.randomUUID(),
        cardName = cardName,
        cardImage = "${cardName.lowercase()}.png",
        currentBid = currentBid,
        remainingDays = remainingDays,
        cardState = CardState.POOR,
        cardCategory = "Pokémon",
        cardCollection = null,
        cardNumber = cardNumber,
    )

    @Test
    @DisplayName("Debe obtener una subasta por su ID si existe")
    fun `should get auction by id`() {
        val auctionId = UUID.randomUUID()
        `when`(auctionRepository.getById(auctionId)).thenReturn(buildSampleAuction(id = auctionId))

        val result = useCase.getById(auctionId)

        assertNotNull(result)
        assertEquals("Gengar", result?.card?.name)
        verify(auctionRepository, times(1)).getById(auctionId)
    }

    @Test
    @DisplayName("Debe retornar null si la subasta no existe por su ID")
    fun `should return null when auction not found`() {
        val auctionId = UUID.randomUUID()
        `when`(auctionRepository.getById(auctionId)).thenReturn(null)

        assertNull(useCase.getById(auctionId))
        verify(auctionRepository, times(1)).getById(auctionId)
    }

    @Test
    @DisplayName("Debe obtener el resumen de todas las subastas con el orden por defecto")
    fun `should get all auction summaries`() {
        val summaries =
            listOf(
                buildSampleSummary(cardName = "Gengar"),
                buildSampleSummary(cardName = "Charizard"),
            )
        `when`(auctionRepository.getAllSummaries(AuctionSort.ENDING_SOON)).thenReturn(summaries)

        val result = useCase.getAll()

        assertEquals(2, result.size)
        assertEquals("Gengar", result[0].cardName)
        assertEquals("Charizard", result[1].cardName)
        verify(auctionRepository, times(1)).getAllSummaries(AuctionSort.ENDING_SOON)
    }

    @Test
    @DisplayName("Debe delegar la ordenación en el repositorio")
    fun `should pass sort to repository`() {
        `when`(auctionRepository.getAllSummaries(AuctionSort.PRICE_ASC))
            .thenReturn(listOf(buildSampleSummary(cardName = "Abra")))

        val result = useCase.getAll(AuctionSort.PRICE_ASC)

        assertEquals("Abra", result.first().cardName)
        verify(auctionRepository, times(1)).getAllSummaries(AuctionSort.PRICE_ASC)
    }
}
