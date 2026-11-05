package com.bestbidder.usecase.port

import com.bestbidder.domain.auction.Auction
import java.time.Instant
import java.util.UUID

/**
 * Puerto de persistencia para la gestión del ciclo de vida de las subastas.
 */
interface AuctionRepository {
    /**
     * Obtiene una subasta por su identificador único.
     */
    fun getById(id: UUID): Auction?

    /**
     * Obtiene todas las subastas registradas.
     */
    fun getAll(): List<Auction>

    /**
     * Recupera el listado de subastas creadas por un usuario determinado.
     */
    fun getByCreator(creator: String): List<Auction>

    /**
     * Recupera las subastas asociadas a una carta específica.
     */
    fun getByCard(cardId: UUID): List<Auction>

    /**
     * Retorna todas las subastas que no han sido canceladas ni finalizadas.
     */
    fun getActive(): List<Auction>

    /**
     * Retorna únicamente las subastas que se encuentran activas en el instante actual.
     */
    fun getInProgress(): List<Auction>

    /**
     * Comprueba si un creador ya tiene una subasta activa o pendiente asociada a una carta.
     */
    fun existsByCreatorAndCard(
        creator: String,
        cardId: UUID,
    ): Boolean

    /**
     * Recupera las subastas cuya fecha de inicio sea igual o anterior al instante dado.
     */
    fun getJustStarted(now: Instant): List<Auction>

    /**
     * Recupera las subastas cuyo tiempo de duración haya expirado respecto al instante dado.
     */
    fun getJustExpired(now: Instant): List<Auction>

    /**
     * Guarda o actualiza una subasta en el almacén de datos.
     */
    fun save(auction: Auction): Auction
}
