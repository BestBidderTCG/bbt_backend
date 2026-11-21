package com.bestbidder.usecase.card

import com.bestbidder.domain.card.Card
import com.bestbidder.domain.card.CardState
import com.bestbidder.usecase.port.CardRepository
import com.bestbidder.usecase.port.FileStoragePort
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.time.Instant
import java.util.UUID

class DeleteCardUseCaseTest {
    private val cardRepository = mock(CardRepository::class.java)
    private val fileStoragePort = mock(FileStoragePort::class.java)
    private val useCase = DeleteCardUseCase(cardRepository, fileStoragePort)

    private val ownerId = UUID.randomUUID()
    private val cardId = UUID.randomUUID()

    private fun existingCard() =
        Card(
            id = cardId,
            ownerId = ownerId,
            name = "Umbreon ex",
            image = "https://res.cloudinary.com/ee6azugw/image/upload/bestbidder/$cardId.jpg",
            category = "Pokémon",
            collection = "Destinos de Paldea",
            num = 17,
            state = CardState.POOR,
            description = "Con pliegues visibles",
            createdAt = Instant.now(),
        )

    @Test
    @DisplayName("Elimina la carta de base de datos y borra su imagen en almacenamiento")
    fun `should delete card and its stored image when owner matches`() {
        `when`(cardRepository.getById(cardId)).thenReturn(existingCard())

        useCase.execute(cardId, ownerId)

        verify(cardRepository).delete(cardId)
        verify(fileStoragePort).delete(cardId)
    }

    @Test
    @DisplayName("Lanza excepción si la carta no existe")
    fun `should throw exception when card does not exist`() {
        `when`(cardRepository.getById(cardId)).thenReturn(null)

        assertThrows(IllegalArgumentException::class.java) {
            useCase.execute(cardId, ownerId)
        }

        verify(cardRepository, never()).delete(cardId)
        verify(fileStoragePort, never()).delete(cardId)
    }

    @Test
    @DisplayName("Lanza excepción y no borra nada si el usuario no es el propietario")
    fun `should throw exception when user is not the owner`() {
        `when`(cardRepository.getById(cardId)).thenReturn(existingCard())

        assertThrows(IllegalArgumentException::class.java) {
            useCase.execute(cardId, UUID.randomUUID())
        }

        verify(cardRepository, never()).delete(cardId)
        verify(fileStoragePort, never()).delete(cardId)
    }
}
