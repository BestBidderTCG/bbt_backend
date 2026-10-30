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
        displayName: String?,
        description: String?,
        avatarNum: String?,
    ): User {
        val current =
            userRepository.findById(userId)
                ?: throw UserNotFoundException(userId)

        val newDisplayName =
            if (displayName != null) {
                require(displayName.isNotBlank()) { "El nombre visible no puede estar vacío" }
                require(displayName.length <= 80) { "El nombre visible no puede superar los 80 caracteres" }
                displayName.trim()
            } else {
                current.displayName
            }

        val newDescription =
            when {
                description == null -> current.description
                description.isBlank() -> null
                description.length > 500 -> throw IllegalArgumentException("La descripción no puede superar los 500 caracteres")
                else -> description.trim()
            }

        val newAvatarNum =
            when {
                avatarNum == null -> current.avatarNum
                avatarNum.isBlank() -> null
                avatarNum.length > 50 -> throw IllegalArgumentException("El avatar no puede superar los 50 caracteres")
                else -> avatarNum.trim()
            }

        return userRepository.save(
            current.copy(
                displayName = newDisplayName,
                description = newDescription,
                avatarNum = newAvatarNum,
            ),
        )
    }
}
