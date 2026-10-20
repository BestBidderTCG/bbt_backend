package com.bestbidder.adapter.persistence.user

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

/**
 * Interfaz de Spring Data JPA.
 * Spring genera las queries SQL basándose en el nombre de los métodos.
 */
interface JpaUserRepository : JpaRepository<UserEntity, UUID> {
    // Spring deberia generar automaticamente el SQL: SELECT * FROM users WHERE username = ?
    fun findByUsername(username: String): UserEntity?
}