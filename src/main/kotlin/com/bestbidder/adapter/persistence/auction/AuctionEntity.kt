package com.bestbidder.adapter.persistence.auction

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

/**
 * Representación de la tabla "auction" en PostgreSQL.
 * Para que JPA gestione la persistencia automáticamente.
 */
@Entity
@Table(name = "aution")
class AuctionEntity(
    @Id
    val id: UUID,
    @Column(name = "creator", nullable = false)
    val creator: String,
    @Column(name = "card", nullable = false)
    val card: String,
    @Column(name = "collection", updatable = false)
    val collection: String,
    @Column(name = "min_prize", nullable = false)
    val min_prize: Double,
)
