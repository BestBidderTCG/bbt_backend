package com.bestbidder.usecase.port

import com.bestbidder.domain.bid.Bid
import java.util.UUID

/**
 * Puerto de persistencia para la gestión y consulta de pujas en subastas.
 */
interface BidRepository {
    /**
     * Recupera la puja con el mayor importe registrado para una subasta.
     */
    fun findHighestBidByAuctionId(auctionId: UUID): Bid?

    /**
     * Retorna el historial completo de pujas de una subasta ordenado descendentemente por importe.
     */
    fun findByAuctionIdOrderByAmountDesc(auctionId: UUID): List<Bid>

    /**
     * Almacena una nueva puja en el sistema.
     */
    fun save(bid: Bid): Bid
}
