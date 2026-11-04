package com.bestbidder.adapter.web.user.dto

import com.bestbidder.domain.user.User
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime
import java.util.UUID

/**
 * DTO  de entrada.
 * Define el JSON exacto que el frontend debe enviarnos en el body.
 */
data class AuthRequest(
    @field:NotBlank(message = "El nombre de usuario es obligatorio")
    @field:Size(min = 3, max = 50, message = "Debe tener entre 3 y 50 caracteres")
    val username: String,
    @field:NotBlank(message = "La contraseña es obligatoria")
    @field:Size(min = 6, max = 100, message = "Debe tener entre 6 y 100 caracteres")
    val password: String,
)

data class RefreshRequest(
    @field:NotBlank(message = "El refresh token es obligatorio")
    val refreshToken: String,
)

data class UpdateUserProfileRequest(
    val displayName: String? = null,
    val description: String? = null,
    val avatarNum: String? = null,
)

data class ChangePasswordRequest(
    @field:NotBlank val currentPassword: String,
    @field:NotBlank @field:Size(min = 6, max = 100) val newPassword: String,
)

data class UserResponse(
    val id: UUID,
    val username: String,
    val displayName: String,
    val description: String?,
    val avatarNum: String?,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun fromDomain(user: User) =
            UserResponse(
                id = user.id,
                username = user.username,
                displayName = user.displayName,
                description = user.description,
                avatarNum = user.avatarNum,
                createdAt = user.createdAt,
            )
    }
}

data class PublicUserResponse(
    val id: UUID,
    val username: String,
    val displayName: String,
    val description: String?,
    val avatarNum: String?,
) {
    companion object {
        fun fromDomain(user: User) =
            PublicUserResponse(
                id = user.id,
                username = user.username,
                displayName = user.displayName,
                description = user.description,
                avatarNum = user.avatarNum,
            )
    }
}

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: UserResponse,
)
