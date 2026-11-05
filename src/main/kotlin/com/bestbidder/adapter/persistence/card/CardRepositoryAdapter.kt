package com.bestbidder.adapter.persistence.card

import com.bestbidder.adapter.persistence.user.JpaUserRepository
import com.bestbidder.domain.card.Card
import com.bestbidder.usecase.port.CardRepository
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class CardRepositoryAdapter(
    private val jpaCardRepository: JpaCardRepository,
    private val jpaUserRepository: JpaUserRepository,
) : CardRepository {
    override fun getById(id: UUID): Card? = jpaCardRepository.findById(id).orElse(null)?.toDomain()

    override fun getAll(): List<Card> = jpaCardRepository.findAll().map { it.toDomain() }

    override fun getByCreator(creatorId: UUID): List<Card> = jpaCardRepository.findByOwnerId(creatorId).map { it.toDomain() }

    override fun save(card: Card): Card {
        val ownerRef = jpaUserRepository.getReferenceById(card.ownerId)
        val entity = CardEntity.fromDomain(card, ownerRef)
        return jpaCardRepository.save(entity).toDomain()
    }
}
