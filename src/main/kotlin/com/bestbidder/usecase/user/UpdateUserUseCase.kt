package com.bestbidder.usecase.user

import com.bestbidder.domain.user.User
import com.bestbidder.domain.user.exceptions.UserNotFoundException
import com.bestbidder.usecase.port.UserRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UpdateUserUseCase(
    private val userRepository: UserRepository,
) {
    fun updateProfile(
        userId: UUID,
        description: String?,
        avatarNum: Int?,
    ): User {
        val current = userRepository.findById(userId) ?: throw UserNotFoundException(userId)

        val newDescription =
            when {
                description == null -> current.description
                description.isBlank() -> null
                description.length > 500 -> throw IllegalArgumentException("La descripción no puede tener más de 500 caracteres")
                else -> description.trim()
            }

        val newAvatarNum =
            if (avatarNum != null) {
                require(avatarNum >= 0) { "El número de avatar no puede ser negativo" }
                avatarNum
            } else {
                current.avatarNum
            }

        return userRepository.save(
            current.copy(description = newDescription, avatarNum = newAvatarNum),
        )
    }
}
