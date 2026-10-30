package com.bestbidder.adapter.persistence.auction

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID

interface JpaAuctionRepository : JpaRepository<AuctionEntity, UUID> {
    fun findByCreatorUsername(creatorUsername: String): List<AuctionEntity>

    fun findByCollection(collection: String): List<AuctionEntity>

    fun findByCard(card: String): List<AuctionEntity>

    /**
     * Devuelve todas las subastas que están activas o próximas (endsAt mayor a la hora actual y no canceladas).
     */
    @Query(
        """
        SELECT a FROM AuctionEntity a 
        WHERE a.isCancelled = false 
          AND a.endsAt > CURRENT_TIMESTAMP 
        ORDER BY a.endsAt ASC
    """,
    )
    fun findActive(): List<AuctionEntity>

    /**
     * Devuelve solo las subastas que están IN_PROGRESS en este instante exacto.
     */
    @Query(
        """
        SELECT a FROM AuctionEntity a 
        WHERE a.isCancelled = false 
          AND a.startedAt <= CURRENT_TIMESTAMP 
          AND a.endsAt > CURRENT_TIMESTAMP 
        ORDER BY a.endsAt ASC
    """,
    )
    fun findInProgress(): List<AuctionEntity>

    /**
     * Comprueba si existe una subasta activa o próxima para esa carta y creador.
     */
    @Query(
        """
        SELECT COUNT(a) > 0 FROM AuctionEntity a 
        WHERE a.creatorUsername = :creator 
          AND a.card = :card 
          AND a.isCancelled = false 
          AND a.endsAt > CURRENT_TIMESTAMP
    """,
    )
    fun existsActiveAuction(
        @Param("creator") creator: String,
        @Param("card") card: String,
    ): Boolean

    /**
     * Devuelve solo las subastas que acaban de empezar en este instante exacto.
     */
    @Query(
        """
        SELECT a FROM AuctionEntity a 
        WHERE a.isCancelled = false 
          AND a.startedAt = :now 
    """,
    )
    fun findJustStarted(
        @Param("now") now: Instant,
    ): List<AuctionEntity>

    /**
     * Devuelve solo las subastas que acaban de expirar en este instante exacto.
     */
    @Query(
        """
        SELECT a FROM AuctionEntity a 
        WHERE a.isCancelled = false 
          AND a.endsAt = :now
    """,
    )
    fun findJustExpired(
        @Param("now") now: Instant,
    ): List<AuctionEntity>
}
