package com.bestbidder.domain.user

import java.time.LocalDateTime
import java.util.UUID

/**
 * Representa la entidad de negocio de un Usuario.
 */
data class User(
    val id: UUID,
    val username: String,
    val passwordHash: String,
    val displayName: String,
    val description: String?,
    // Cambiar prob en algun momento a numero
    val avatarNum: String?,
    val createdAt: LocalDateTime = LocalDateTime.now(),
)
