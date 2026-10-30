package com.bestbidder.usecase.user

import com.bestbidder.domain.user.User
import com.bestbidder.domain.user.exceptions.InvalidCredentialsException
import com.bestbidder.domain.user.exceptions.UserNotFoundException
import com.bestbidder.domain.user.exceptions.UsernameAlreadyExistsException
import com.bestbidder.infrastructure.config.JwtService
import com.bestbidder.infrastructure.security.RefreshTokenService
import com.bestbidder.usecase.port.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.UUID

/**
 * Caso de uso encargado de la lógica de negocio de Autenticación
 */
@Service
class AuthUseCase(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val refreshTokenService: RefreshTokenService,
) {
    fun register(
        username: String,
        password: String,
    ): User {
        // 1. Comprobar si ya existe
        if (userRepository.existsByUsername(username)) {
            throw UsernameAlreadyExistsException(username)
        }

        // 2. Crear usuario con contraseña encriptada
        val newUser =
            User(
                id = UUID.randomUUID(),
                username = username,
                passwordHash = passwordEncoder.encode(password)!!,
                displayName = username,
                description = null,
                avatarNum = null,
            )

        // 3. Guardar
        return userRepository.save(newUser)
    }

    fun login(
        username: String,
        password: String,
    ): AuthResult {
        // 1. Buscar usuario
        val user =
            userRepository.findByUsername(username)
                ?: throw InvalidCredentialsException()

        // 2. Comprobar la contraseña
        if (!passwordEncoder.matches(password, user.passwordHash)) {
            throw InvalidCredentialsException()
        }

        return issueTokens(user)
    }

    fun refresh(refreshToken: String): AuthResult {
        val userId = refreshTokenService.validateAndConsume(refreshToken)
        val user = userRepository.findById(userId) ?: throw UserNotFoundException(userId)
        return issueTokens(user)
    }

    fun logout(refreshToken: String) {
        refreshTokenService.revoke(refreshToken)
    }

    private fun issueTokens(user: User): AuthResult {
        val access = jwtService.generateAccessToken(user.id, user.username)
        val refresh = refreshTokenService.generate(user.id)
        return AuthResult(user, access, refresh)
    }
}
