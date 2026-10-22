package com.bestbidder.adapter.persistence.auction

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

/**
 * Interfaz de Spring Data JPA.
 * Spring genera las queries SQL basándose en el nombre de los métodos.
 */
interface JpaAuctionRepository : JpaRepository<AuctionEntity, UUID> {
    // Spring deberia generar automaticamente el SQL: SELECT * FROM auction WHERE id = ?
    // Spring deberia generar automaticamente el SQL: SELECT * FROM auction WHERE creator = ?
    // Spring deberia generar automaticamente el SQL: SELECT * FROM auction WHERE collection = ?
    // Spring deberia generar automaticamente el SQL: SELECT * FROM auction WHERE card = ?
}
