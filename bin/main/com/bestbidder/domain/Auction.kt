package com.bestbidder.domain.auction

import java.time.Duration
import java.time.Instant
import java.util.UUID

/**
 * Representa la entidad de negocio de una Subasta.
 * Es inmutable y calcula su estado de forma dinámica en función del tiempo.
 */
data class Auction(
    val id: UUID = UUID.randomUUID(),
    val creator_id: UUID,
    val creator_username: String,
    val card: String,
    val collection: String,
    val min_prize: Double,
    val startedAt: Instant,
    val durationInDays: Int,
    val isCancelled: Boolean = false,
) {
    /**
     * Fecha exacta de finalización calculada a partir de la fecha de inicio y la duración.
     */
    val endsAt: Instant = startedAt.plus(Duration.ofDays(durationInDays.toLong()))

    init {
        require(durationInDays in 1..7) {
            "La duración de la subasta debe estar entre 1 y 7 días"
        }
        require(min_prize > 0.0) {
            "El precio mínimo debe ser mayor a 0"
        }
    }

    /**
     * Estado dinámico de la subasta en el momento actual del sistema.
     */
    val status: AuctionStatus
        get() = getStatusAt(Instant.now())

    /**
     * Permite evaluar el estado en un punto temporal específico.
     * Útil para tests unitarios o simulaciones de tiempo.
     */
    fun getStatusAt(now: Instant): AuctionStatus {
        if (isCancelled) return AuctionStatus.CANCELLED
        if (now.isBefore(startedAt)) return AuctionStatus.UPCOMING
        if (now.isBefore(endsAt)) return AuctionStatus.IN_PROGRESS
        return AuctionStatus.FINISHED
    }

    /**
     * Indica si la subasta está activa o pendiente de empezar (UPCOMING o IN_PROGRESS).
     */
    fun isPendingOrActive(now: Instant = Instant.now()): Boolean {
        val currentStatus = getStatusAt(now)
        return currentStatus == AuctionStatus.UPCOMING || currentStatus == AuctionStatus.IN_PROGRESS
    }

    /**
     * Indica que la subasta está cancelada.
     */
    fun cancel(): Auction = copy(isCancelled = true)
}
