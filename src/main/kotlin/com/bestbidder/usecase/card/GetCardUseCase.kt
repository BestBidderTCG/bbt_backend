package com.bestbidder.usecase.card

import com.bestbidder.domain.card.Card
import com.bestbidder.usecase.port.CardRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class GetCardUseCase(
    private val cardRepository: CardRepository,
) {
    fun getById(id: UUID): Card? = cardRepository.getById(id)

    fun getAll(): List<Card> = cardRepository.getAll()

    fun getByOwner(ownerId: UUID): List<Card> = cardRepository.getByCreator(ownerId)
}
