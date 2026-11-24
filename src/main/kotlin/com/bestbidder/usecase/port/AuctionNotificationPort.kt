package com.bestbidder.usecase.port

import com.bestbidder.domain.bid.Bid
import java.util.UUID

/**
 * Puerto de salida para el envío de notificaciones de eventos en subastas.
 *
 * Define las operaciones necesarias para informar a los usuarios y sistemas
 * externos sobre los cambios de estado de una subasta y la actividad de pujas.
 */
interface AuctionNotificationPort {
    /**
     * Notifica que una subasta ha pasado a estado activo o en curso.
     */
    fun notifyAuctionStarted(auctionId: UUID)

    /**
     * Notifica que el tiempo límite de una subasta ha finalizado.
     * [winningBid] es la puja más alta, o null si nadie pujó.
     */
    fun notifyAuctionExpired(
        auctionId: UUID,
        winningBid: Bid?,
    )

    /**
     * Notifica que una subasta ha sido cancelada por su creador o un administrador.
     */
    fun notifyAuctionCancelled(auctionId: UUID)

    /**
     * Notifica que se ha registrado una nueva puja válida en la subasta.
     */
    fun notifyNewBidPlaced(
        auctionId: UUID,
        newAmount: Double,
        bidderUsername: String,
    )
}
