package com.bestbidder.usecase.card

import com.bestbidder.domain.card.Card
import com.bestbidder.domain.card.CardState
import com.bestbidder.usecase.port.CardRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.time.Instant
import java.util.UUID

class GetCardUseCaseTest {
    private val cardRepository = mock(CardRepository::class.java)
    private val useCase = GetCardUseCase(cardRepository)

    private fun buildCard(
        id: UUID = UUID.randomUUID(),
        ownerId: UUID = UUID.randomUUID(),
    ) = Card(
        id = id,
        ownerId = ownerId,
        name = "Pikachu ex",
        image = "https://res.cloudinary.com/ee6azugw/image/upload/bestbidder/pikachu.jpg",
        category = "Pokémon",
        collection = "Surging Sparks",
        num = 238,
        state = CardState.NEAR_MINT,
        description = "Descripción de prueba",
        createdAt = Instant.now(),
    )

    @Test
    @DisplayName("Devuelve todas las cartas registradas")
    fun `should return all cards`() {
        val cards = listOf(buildCard(), buildCard())
        `when`(cardRepository.getAll()).thenReturn(cards)

        val result = useCase.getAll()

        assertEquals(2, result.size)
        assertEquals(cards, result)
    }

    @Test
    @DisplayName("Devuelve null si la carta solicitada por ID no existe")
    fun `should return null when card does not exist`() {
        val cardId = UUID.randomUUID()
        `when`(cardRepository.getById(cardId)).thenReturn(null)

        val result = useCase.getById(cardId)

        assertNull(result)
    }

    @Test
    @DisplayName("Devuelve la carta cuando existe en el repositorio")
    fun `should return card when it exists`() {
        val cardId = UUID.randomUUID()
        val card = buildCard(id = cardId)
        `when`(cardRepository.getById(cardId)).thenReturn(card)

        val result = useCase.getById(cardId)

        assertEquals(card, result)
    }
}
