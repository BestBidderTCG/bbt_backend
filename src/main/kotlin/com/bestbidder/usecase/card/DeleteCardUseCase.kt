package com.bestbidder.usecase.card

import com.bestbidder.usecase.port.CardRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class DeleteCardUseCase(private val cardRepository: CardRepository) {
    fun execute(
        cardId: UUID,
        ownerId: UUID,
    ) {
        val existing =
            cardRepository.getById(cardId)
                ?: throw IllegalArgumentException("Carta no encontrada")

        require(existing.ownerId == ownerId) {
            "No tienes permiso para borrar esta carta"
        }

        cardRepository.delete(cardId)
    }
}
