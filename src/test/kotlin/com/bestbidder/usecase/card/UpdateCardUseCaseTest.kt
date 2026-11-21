package com.bestbidder.usecase.card

import com.bestbidder.domain.card.Card
import com.bestbidder.domain.card.CardState
import com.bestbidder.usecase.port.CardRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.time.Instant
import java.util.UUID

class UpdateCardUseCaseTest {
    private val cardRepository = mock(CardRepository::class.java)
    private val useCase = UpdateCardUseCase(cardRepository)

    private val ownerId = UUID.randomUUID()
    private val cardId = UUID.randomUUID()

    private fun buildCard(
        name: String = "Black Lotus",
        collection: String? = "Alpha / Beta / Unlimited",
        num: Int? = 232,
        state: CardState = CardState.GOOD,
        description: String? = "Descripción original",
    ) = Card(
        id = cardId,
        ownerId = ownerId,
        name = name,
        image = "https://res.cloudinary.com/ee6azugw/image/upload/bestbidder/$cardId.jpg",
        category = "Magic",
        collection = collection,
        num = num,
        state = state,
        description = description,
        createdAt = Instant.now(),
    )

    @Test
    @DisplayName("Actualiza los datos de la carta cuando el propietario coincide")
    fun `should update card when owner matches`() {
        val current = buildCard()
        val updated =
            buildCard(
                name = "Mox Diamond",
                collection = "Ciclo de Urza (1998-1999)",
                num = 138,
                state = CardState.LIGHT_PLAYED,
                description = "Nueva descripción",
            )

        `when`(cardRepository.getById(cardId)).thenReturn(current)
        `when`(cardRepository.save(any())).thenAnswer { it.getArgument<Card>(0) }

        val result = useCase.execute(cardId, ownerId, updated)

        assertEquals("Mox Diamond", result.name)
        assertEquals("Ciclo de Urza (1998-1999)", result.collection)
        assertEquals(138, result.num)
        assertEquals(CardState.LIGHT_PLAYED, result.state)

        verify(cardRepository).save(any())
    }

    @Test
    @DisplayName("Lanza excepción si la carta no existe")
    fun `should throw exception when card does not exist`() {
        `when`(cardRepository.getById(cardId)).thenReturn(null)

        assertThrows(IllegalArgumentException::class.java) {
            useCase.execute(cardId, ownerId, buildCard())
        }

        verify(cardRepository, never()).save(any())
    }

    @Test
    @DisplayName("Lanza excepción si el usuario no es el propietario de la carta")
    fun `should throw exception when user is not the owner`() {
        `when`(cardRepository.getById(cardId)).thenReturn(buildCard())

        assertThrows(IllegalArgumentException::class.java) {
            useCase.execute(cardId, UUID.randomUUID(), buildCard())
        }

        verify(cardRepository, never()).save(any())
    }
}
