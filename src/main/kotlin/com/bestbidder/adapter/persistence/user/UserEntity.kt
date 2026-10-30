package com.bestbidder.adapter.persistence.user

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

/**
 * Representación de la tabla "user" en PostgreSQL.
 * Para que JPA gestione la persistencia automáticamente.
 */
@Entity
@Table(name = "users")
class UserEntity(
    @Id
    val id: UUID,
    @Column(unique = true, nullable = false, length = 50)
    val username: String,
    @Column(name = "password_hash", nullable = false)
    val passwordHash: String,
    @Column(name = "display_name", nullable = false, length = 80)
    val displayName: String,
    @Column(name = "description", length = 500)
    val description: String?,
    @Column(name = "avatar_num", length = 50)
    val avatarNum: String?,
    @Column(name = "created_at", updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
)
