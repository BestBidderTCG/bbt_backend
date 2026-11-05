package com.bestbidder.usecase.auction

import com.bestbidder.domain.auction.Auction
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

    @Test
    @DisplayName("Debe obtener una subasta por su ID si existe")
    fun `should get auction by id`() {
        val auctionId = UUID.randomUUID()
        val creatorId = UUID.randomUUID()
        val expected =
            Auction(
                id = auctionId,
                creator_id = creatorId,
                creator_username = "paula",
                card =
                    Card(
                        id = UUID.randomUUID(),
                        ownerId = creatorId,
                        name = "Gengar",
                        image = "gengar.png",
                        category = "Pokémon",
                        state = CardState.POOR,
                    ),
                min_prize = 80.0,
                startedAt = Instant.now(),
                durationInDays = 3,
            )

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
}
