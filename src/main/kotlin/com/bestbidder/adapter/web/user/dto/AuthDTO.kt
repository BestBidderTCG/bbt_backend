package com.bestbidder.adapter.web.user.dto

import com.bestbidder.domain.user.User
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime
import java.util.UUID

// DTO de entrada. Define el JSON exacto que el frontend debe enviarnos en el body.

// Para el Login
data class LoginRequest(
    @field:NotBlank(message = "El email o username es obligatorio")
    val identifier: String,
    @field:NotBlank(message = "La contraseña es obligatoria")
    val password: String,
)

// Para el Registro
data class RegisterRequest(
    @field:NotBlank(message = "El email es obligatorio")
    @field:Email(message = "El formato del email no es válido")
    val email: String,
    @field:NotBlank(message = "El username es obligatorio")
    val username: String,
    @field:NotBlank(message = "La contraseña es obligatoria")
    @field:Size(min = 6, max = 100)
    val password: String,
)

// Para Editar Perfil
data class UpdateUserProfileRequest(
    val description: String? = null,
    val avatarNum: Int? = null,
)

data class ChangePasswordRequest(
    @field:NotBlank val currentPassword: String,
    @field:NotBlank @field:Size(min = 6, max = 100) val newPassword: String,
)

// Respuestas
data class UserResponse(
    val id: UUID,
    val email: String,
    val username: String,
    val description: String?,
    val avatarNum: Int?,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun fromDomain(user: User) =
            UserResponse(
                id = user.id,
                email = user.email,
                username = user.username,
                description = user.description,
                avatarNum = user.avatarNum,
                createdAt = user.createdAt,
            )
    }
}

// Perfil público
data class PublicUserResponse(
    val id: UUID,
    val username: String,
    val email: String,
    val description: String?,
    val avatarNum: Int?,
) {
    companion object {
        fun fromDomain(user: User) =
            PublicUserResponse(
                id = user.id,
                username = user.username,
                email = user.email,
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

data class RefreshRequest(
    @field:NotBlank(message = "El refresh token es obligatorio")
    val refreshToken: String,
)
