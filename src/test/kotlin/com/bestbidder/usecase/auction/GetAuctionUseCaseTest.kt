package com.bestbidder.usecase.auction

import com.bestbidder.domain.auction.Auction
import com.bestbidder.domain.auction.AuctionCreator
import com.bestbidder.domain.auction.AuctionDetail
import com.bestbidder.domain.auction.AuctionListItem
import com.bestbidder.domain.auction.AuctionStatus
import com.bestbidder.domain.auction.UserAuctionSummary
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
    ) = AuctionListItem(
        id = UUID.randomUUID(),
        cardName = cardName,
        cardImage = "${cardName.lowercase()}.png",
        currentBid = currentBid,
        remainingDays = remainingDays,
        cardState = CardState.POOR,
        cardCategory = "Pokémon",
        cardCollection = null,
        cardNumber = cardNumber,
        status = AuctionStatus.IN_PROGRESS,
    )

    private fun buildSampleUserSummary(
        cardName: String = "Gengar",
        status: AuctionStatus = AuctionStatus.IN_PROGRESS,
        winnerUsername: String? = null,
    ) = UserAuctionSummary(
        id = UUID.randomUUID(),
        cardName = cardName,
        cardImage = "${cardName.lowercase()}.png",
        status = status,
        minPrize = 80.0,
        currentBid = 120.0,
        startedAt = 1_700_000_000_000,
        endsAt = 1_700_259_200_000,
        winnerUsername = winnerUsername,
    )

    private fun buildSampleDetail(
        creator: AuctionCreator? = AuctionCreator(UUID.randomUUID(), "paula", 1),
        isFavorite: Boolean? = false,
    ) = AuctionDetail(
        id = UUID.randomUUID(),
        status = AuctionStatus.IN_PROGRESS,
        cardName = "Gengar",
        cardImage = "gengar.png",
        cardCollection = null,
        cardNumber = null,
        cardState = CardState.POOR,
        minPrize = 80.0,
        increment = 1.0,
        lastBid = null,
        bidCount = 0,
        startedAt = Instant.now(),
        endsAt = Instant.now().plusSeconds(3600),
        description = null,
        creator = creator,
        isFavorite = isFavorite,
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

    @Test
    @DisplayName("Debe obtener las subastas del usuario delegando en el repositorio")
    fun `should get user auctions`() {
        val userId = UUID.randomUUID()
        val summaries =
            listOf(
                buildSampleUserSummary(cardName = "Gengar", status = AuctionStatus.IN_PROGRESS),
                buildSampleUserSummary(
                    cardName = "Charizard",
                    status = AuctionStatus.FINISHED,
                    winnerUsername = "ganador",
                ),
            )
        `when`(auctionRepository.getUserAuctions(userId)).thenReturn(summaries)

        val result = useCase.getUserAuctions(userId)

        assertEquals(2, result.size)
        assertEquals("Gengar", result[0].cardName)
        assertEquals(AuctionStatus.IN_PROGRESS, result[0].status)
        assertNull(result[0].winnerUsername)
        assertEquals("Charizard", result[1].cardName)
        assertEquals(AuctionStatus.FINISHED, result[1].status)
        assertEquals("ganador", result[1].winnerUsername)
        verify(auctionRepository, times(1)).getUserAuctions(userId)
    }

    @Test
    @DisplayName("Debe retornar lista vacía si el usuario no tiene subastas")
    fun `should return empty list when user has no auctions`() {
        val userId = UUID.randomUUID()
        `when`(auctionRepository.getUserAuctions(userId)).thenReturn(emptyList())

        val result = useCase.getUserAuctions(userId)

        assertTrue(result.isEmpty())
        verify(auctionRepository, times(1)).getUserAuctions(userId)
    }

    @Test
    @DisplayName("Debe obtener el detalle de una subasta delegando en el repositorio")
    fun `should get auction detail`() {
        val auctionId = UUID.randomUUID()
        val viewerId = UUID.randomUUID()
        val detail = buildSampleDetail(isFavorite = true)
        `when`(auctionRepository.getDetailById(auctionId, viewerId)).thenReturn(detail)

        val result = useCase.getDetail(auctionId, viewerId)

        assertEquals(detail, result)
        verify(auctionRepository, times(1)).getDetailById(auctionId, viewerId)
    }

    @Test
    @DisplayName("Debe pasar viewerId null al repositorio si no hay usuario autenticado")
    fun `should get auction detail without viewer`() {
        val auctionId = UUID.randomUUID()
        `when`(auctionRepository.getDetailById(auctionId, null))
            .thenReturn(buildSampleDetail(isFavorite = null))

        val result = useCase.getDetail(auctionId, null)

        assertNull(result?.isFavorite)
        verify(auctionRepository, times(1)).getDetailById(auctionId, null)
    }

    @Test
    @DisplayName("Debe retornar null si la subasta no existe")
    fun `should return null detail when auction not found`() {
        val auctionId = UUID.randomUUID()
        `when`(auctionRepository.getDetailById(auctionId, null)).thenReturn(null)

        assertNull(useCase.getDetail(auctionId, null))
    }
}
