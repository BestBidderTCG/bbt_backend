package com.bestbidder.usecase.card

import com.bestbidder.domain.card.Card
import com.bestbidder.usecase.port.CardRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UpdateCardUseCase(private val cardRepository: CardRepository) {
    fun execute(cardId: UUID, ownerId: UUID, updatedCard: Card): Card {
        val existing = cardRepository.getById(cardId)
            ?: throw IllegalArgumentException("Carta no encontrada")

        require(existing.ownerId == ownerId) {
            "No tienes permiso para editar esta carta"
        }

        // Se guarda con el mismo ID y dueño
        val cardToSave = updatedCard.copy(id = cardId, ownerId = ownerId)
        return cardRepository.save(cardToSave)
    }
}