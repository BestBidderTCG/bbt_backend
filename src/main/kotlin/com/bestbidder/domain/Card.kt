package com.bestbidder.domain.card

/**
 * Representa la entidad de negocio de una Carta.
 */
data class Card(
    val id: String,
    val name: String,
    val description: String,
    val image: String,
    val collection: String,
)
