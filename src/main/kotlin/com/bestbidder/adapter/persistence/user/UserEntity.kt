package com.bestbidder.adapter.persistence.user

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

/**
 * Representación de la tabla "users" en PostgreSQL.
 * Para que JPA gestione la persistencia automáticamente.
 */
@Entity
@Table(name = "users")
class UserEntity(
    @Id
    val id: UUID,
    
    @Column(unique = true, nullable = false)
    val username: String,
    
    @Column(name = "password_hash", nullable = false)
    val passwordHash: String,
    
    @Column(name = "created_at", updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)