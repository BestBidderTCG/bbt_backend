package com.bestbidder.domain.auction

import com.bestbidder.domain.card.CardState
import java.time.Instant
import java.util.UUID

data class AuctionCreator(
    val id: UUID,
    val username: String,
    val avatarNum: Int?,
)

/**
 * Detalle completo de una subasta.
 * @property lastBid importe de la última puja; null si aún no hay pujas.
 * @property creator creador de la subasta; null si quien consulta es el propietario.
 * @property isFavorite null si quien consulta es el propietario o no está autenticado.
 */
data class AuctionDetail(
    val id: UUID,
    val status: AuctionStatus,
    val cardName: String,
    val cardImage: String,
    val cardCollection: String?,
    val cardNumber: Int?,
    val cardState: CardState,
    val minPrize: Double,
    val increment: Double,
    val lastBid: Double?,
    val bidCount: Long,
    val startedAt: Instant,
    val endsAt: Instant,
    val description: String?,
    val creator: AuctionCreator?,
    val isFavorite: Boolean?,
)
