package com.bestbidder.adapter.web.user.dto

import java.util.UUID

/**
 * DTO  de entrada.
 * Define el JSON exacto que el frontend debe enviarnos en el body.
 */
data class AuthRequest(
    val username: String,
    val password: String
)

data class UserResponse(
    val id: UUID,
    val username: String
)