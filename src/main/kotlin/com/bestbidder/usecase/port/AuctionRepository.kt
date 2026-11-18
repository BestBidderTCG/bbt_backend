package com.bestbidder.usecase.port

import com.bestbidder.domain.auction.Auction
import com.bestbidder.domain.auction.AuctionSummary
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
     * Obtiene una subasta bloqueando el registro para escritura hasta el fin de la transacción.
     */
    fun getByIdForUpdate(id: UUID): Auction?

    /**
     * Obtiene todas las subastas registradas.
     */
    fun getAllSummaries(): List<AuctionSummary>

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

    fun hasActiveAuctionsAsCreator(userId: UUID): Boolean
}
