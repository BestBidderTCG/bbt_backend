package com.bestbidder.domain.user

import java.time.Instant
import java.time.LocalDateTime
import java.util.UUID

/**
 * Representa la entidad de negocio de un Usuario.
 */
data class User(
    val id: UUID,
    val email: String,
    val passwordHash: String,
    val username: String,
    val description: String?,
    // Cambiar prob en algun momento a numero (llegó el dia)
    val avatarNum: Int?,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val deletionRequestedAt: Instant? = null,
    val deletedAt: Instant? = null,
) {
    val isDeleted: Boolean get() = deletedAt != null
    val isPendingDeletion: Boolean get() = deletionRequestedAt != null && deletedAt == null
}
