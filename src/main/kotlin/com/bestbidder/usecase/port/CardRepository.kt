package com.bestbidder.usecase.port

import com.bestbidder.domain.card.Card
import java.util.UUID

/**
 * Puerto de persistencia para el catálogo de cartas coleccionables.
 */
interface CardRepository {
    /**
     * Busca una carta por su identificador único.
     */
    fun getById(id: UUID): Card?

    /**
     * Obtiene el inventario completo de cartas registradas.
     */
    fun getAll(): List<Card>

    /**
     * Obtiene las cartas que pertenecen a un propietario concreto.
     */
    fun getByCreator(creatorId: UUID): List<Card>

    /**
     * Guarda o actualiza la información de una carta.
     */
    fun save(card: Card): Card
}
