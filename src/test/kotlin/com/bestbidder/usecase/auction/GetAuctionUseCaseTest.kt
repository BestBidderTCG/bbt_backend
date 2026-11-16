package com.bestbidder.usecase.auction

import com.bestbidder.domain.auction.Auction
import com.bestbidder.domain.card.Card
import com.bestbidder.domain.card.CardState
import com.bestbidder.usecase.port.AuctionRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

class GetAuctionUseCaseTest {
    private val auctionRepository: AuctionRepository = mock(AuctionRepository::class.java)
    private val useCase = GetAuctionUseCase(auctionRepository)

    private fun buildSampleAuction(
        id: UUID = UUID.randomUUID(),
        creatorUsername: String = "paula",
        cardName: String = "Gengar",
        minPrize: Double = 80.0,
    ): Auction {
        val creatorId = UUID.randomUUID()
        return Auction(
            id = id,
            creatorId = creatorId,
            creatorUsername = creatorUsername,
            card =
                Card(
                    id = UUID.randomUUID(),
                    ownerId = creatorId,
                    name = cardName,
                    image = "${cardName.lowercase()}.png",
                    category = "Pokémon",
                    state = CardState.POOR,
                ),
            minPrize = minPrize,
            startedAt = Instant.now(),
            durationInDays = 3,
        )
    }

    @Test
    @DisplayName("Debe obtener una subasta por su ID si existe")
    fun `should get auction by id`() {
        val auctionId = UUID.randomUUID()
        val expected = buildSampleAuction(id = auctionId)

        `when`(auctionRepository.getById(auctionId)).thenReturn(expected)

        val result = useCase.getById(auctionId)

        assertNotNull(result)
        assertEquals("Gengar", result?.card?.name)
        assertEquals("Pokémon", result?.card?.category)
        verify(auctionRepository, times(1)).getById(auctionId)
    }

    @Test
    @DisplayName("Debe retornar null si la subasta no existe por su ID")
    fun `should return null when auction not found`() {
        val auctionId = UUID.randomUUID()
        `when`(auctionRepository.getById(auctionId)).thenReturn(null)

        val result = useCase.getById(auctionId)

        assertNull(result)
        verify(auctionRepository, times(1)).getById(auctionId)
    }

    @Test
    @DisplayName("Debe obtener todas las subastas registradas")
    fun `should get all auctions`() {
        val auctions =
            listOf(
                buildSampleAuction(cardName = "Gengar"),
                buildSampleAuction(cardName = "Charizard"),
            )
        `when`(auctionRepository.getAll()).thenReturn(auctions)

        val result = useCase.getAll()

        assertEquals(2, result.size)
        assertEquals("Gengar", result[0].card.name)
        assertEquals("Charizard", result[1].card.name)
        verify(auctionRepository, times(1)).getAll()
    }

    @Test
    @DisplayName("Debe buscar subastas aplicando los criterios de filtrado")
    fun `should search auctions with criteria`() {
        val criteria =
            AuctionSearchCriteria(
                query = "Charizard",
                onlyActive = true,
                minPrice = BigDecimal("50.00"),
                maxPrice = BigDecimal("150.00"),
            )
        val expected = listOf(buildSampleAuction(cardName = "Charizard", minPrize = 100.0))

        `when`(auctionRepository.search(criteria)).thenReturn(expected)

        val result = useCase.search(criteria)

        assertEquals(1, result.size)
        assertEquals("Charizard", result.first().card.name)
        verify(auctionRepository, times(1)).search(criteria)
    }

    @Test
    @DisplayName("Debe retornar una lista vacía si ninguna subasta coincide con la búsqueda")
    fun `should return empty list when no auctions match search criteria`() {
        val criteria = AuctionSearchCriteria(query = "Mewtwo")
        `when`(auctionRepository.search(criteria)).thenReturn(emptyList())

        val result = useCase.search(criteria)

        assertTrue(result.isEmpty())
        verify(auctionRepository, times(1)).search(criteria)
    }
}
