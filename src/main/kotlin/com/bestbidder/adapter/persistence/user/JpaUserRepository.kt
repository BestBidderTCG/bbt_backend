package com.bestbidder.adapter.persistence.user

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID

/**
 * Interfaz de Spring Data JPA.
 * Spring genera las queries SQL basándose en el nombre de los métodos.
 */
interface JpaUserRepository : JpaRepository<UserEntity, UUID> {
    // Spring deberia generar automaticamente el SQL: SELECT * FROM users WHERE username = ?
    fun findByUsername(username: String): UserEntity?

    fun existsByUsername(username: String): Boolean

    fun findByEmail(email: String): UserEntity?

    fun existsByEmail(email: String): Boolean

    fun findByDeletionRequestedAtBeforeAndDeletedAtIsNull(cutoff: Instant): List<UserEntity>

    // El WHERE hace la comprobación de saldo y el descuento en una sola operación atómica
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE UserEntity u SET u.balance = u.balance - :amount WHERE u.id = :id AND u.balance >= :amount")
    fun deductBalance(
        @Param("id") id: UUID,
        @Param("amount") amount: Double,
    ): Int

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE UserEntity u SET u.balance = u.balance + :amount WHERE u.id = :id")
    fun addBalance(
        @Param("id") id: UUID,
        @Param("amount") amount: Double,
    ): Int

    @Query("SELECT u.balance FROM UserEntity u WHERE u.id = :id")
    fun findBalanceById(
        @Param("id") id: UUID,
    ): Double?
}
