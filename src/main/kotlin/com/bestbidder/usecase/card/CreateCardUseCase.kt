package com.bestbidder.usecase.card

import com.bestbidder.domain.card.Card
import com.bestbidder.domain.card.CardState
import com.bestbidder.usecase.port.CardRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class CreateCardUseCase(
    private val cardRepository: CardRepository,
) {
    fun execute(
        ownerId: UUID,
        name: String,
        image: String,
        category: String,
        collection: String? = null,
        num: Int? = null,
        state: CardState,
        description: String? = null,
    ): Card {
        val newCard =
            Card(
                ownerId = ownerId,
                name = name,
                image = image,
                category = category,
                collection = collection,
                num = num,
                state = state,
                description = description,
            )

        return cardRepository.save(newCard)
    }
}
