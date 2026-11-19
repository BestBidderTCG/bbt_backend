package com.bestbidder.usecase.port

import com.bestbidder.domain.auction.Auction
import com.bestbidder.domain.auction.AuctionDetail
import com.bestbidder.domain.auction.AuctionListItem
import com.bestbidder.domain.auction.UserAuctionSummary
import com.bestbidder.usecase.auction.AuctionSort
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

    fun getDetailById(
        id: UUID,
        viewerId: UUID?,
    ): AuctionDetail?

    /**
     * Obtiene una subasta bloqueando el registro para escritura hasta el fin de la transacción.
     */
    fun getByIdForUpdate(id: UUID): Auction?

    /**
     * Obtiene todas las subastas registradas.
     */
    fun getAllSummaries(sort: AuctionSort): List<AuctionListItem>

    /**
     * Obtiene las subastas creadas por el usuario logueado.
     */
    fun getUserAuctions(id: UUID): List<UserAuctionSummary>

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
