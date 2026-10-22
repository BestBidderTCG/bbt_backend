package com.bestbidder.domain.user

import java.util.UUID

/**
 * Representa la entidad de negocio de un Usuario.
 */
data class User(
    val id: UUID,
    val username: String,
    val passwordHash: String,
)
