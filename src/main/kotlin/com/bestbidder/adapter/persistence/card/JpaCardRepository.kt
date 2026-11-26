package com.bestbidder.adapter.persistence.card

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface JpaCardRepository : JpaRepository<CardEntity, UUID> {
    fun findByOwnerId(ownerId: UUID): List<CardEntity>
}
