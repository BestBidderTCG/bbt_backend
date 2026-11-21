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

class CreateCardUseCaseTest {
    private val cardRepository = mock(CardRepository::class.java)
    private val useCase = CreateCardUseCase(cardRepository)

    private val ownerId = UUID.randomUUID()
    private val imageUrl = "https://res.cloudinary.com/ee6azugw/image/upload/bestbidder/card.jpg"

    @Test
    @DisplayName("Crea una carta válida y la guarda en el repositorio")
    fun `should create and save card when data is valid`() {
        `when`(cardRepository.save(any())).thenAnswer { invocation ->
            val card = invocation.getArgument<Card>(0)
            card.copy(createdAt = Instant.now())
        }

        val result =
            useCase.execute(
                ownerId = ownerId,
                name = "Charizard ex",
                image = imageUrl,
                category = "Pokémon",
                collection = "Pokémon 151",
                num = 6,
                state = CardState.NEAR_MINT,
                description = "Carta mítica en excelente estado.",
            )

        assertEquals("Charizard ex", result.name)
        assertEquals(ownerId, result.ownerId)
        assertEquals(imageUrl, result.image)
        assertEquals("Pokémon", result.category)
        assertEquals("Pokémon 151", result.collection)
        assertEquals(6, result.num)
        assertEquals(CardState.NEAR_MINT, result.state)

        verify(cardRepository).save(any())
    }

    @Test
    @DisplayName("Falla si la combinación de categoría y colección no existe en el catálogo")
    fun `should throw exception when category or collection is invalid`() {
        assertThrows(IllegalArgumentException::class.java) {
            useCase.execute(
                ownerId = ownerId,
                name = "Charizard ex",
                image = imageUrl,
                category = "Pokémon",
                collection = "Colección Inventada",
                num = 6,
                state = CardState.NEAR_MINT,
                description = null,
            )
        }

        verify(cardRepository, never()).save(any())
    }
}
