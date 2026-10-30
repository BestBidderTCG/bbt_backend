package com.bestbidder.usecase.user

import com.bestbidder.domain.user.exceptions.InvalidCredentialsException
import com.bestbidder.domain.user.exceptions.UserNotFoundException
import com.bestbidder.infrastructure.security.RefreshTokenService
import com.bestbidder.usecase.port.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ChangePasswordUseCase(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val refreshTokenService: RefreshTokenService,
) {
    fun change(
        userId: UUID,
        currentPassword: String,
        newPassword: String,
    ) {
        val user =
            userRepository.findById(userId)
                ?: throw UserNotFoundException(userId)

        if (!passwordEncoder.matches(currentPassword, user.passwordHash)) {
            throw InvalidCredentialsException()
        }
        require(newPassword.length in 6..100) { "La nueva contraseña debe tener entre 6 y 100 caracteres" }

        userRepository.save(user.copy(passwordHash = passwordEncoder.encode(newPassword)!!))
        refreshTokenService.revokeAll(userId)
    }
}
