package com.bestbidder.domain.card

import java.util.UUID

/**
 * Representa la entidad de negocio de una Carta.
 */
data class Card(
    val id: UUID = UUID.randomUUID(),
    val ownerId: UUID,
    val name: String,
    val image: String,
    val category: String,
    val collection: String? = null,
    val num: Int? = null,
    val state: CardState,
    val description: String? = null,
)
