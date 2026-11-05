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
import org.springframework.transaction.annotation.Transactional
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
    // REGLAS pa validar los nombres y gmails
    private val usernameE = "^(?=.*[a-z])[a-z0-9_.]{3,50}$".toRegex()
    private val emailL = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[a-zA-Z]{2,}$".toRegex()

    fun register(
        email: String,
        username: String,
        password: String,
    ): User {
        // 1. Validar formatos
        if (!emailL.matches(email)) {
            throw IllegalArgumentException("El formato del email no es válido.")
        }
        if (!usernameE.matches(username)) {
            throw IllegalArgumentException(
                "El username debe tener entre 3 y 50 caracteres, solo minúsculas, " +
                    "números, guion bajo o punto, y contener al menos una letra.",
            )
        }

        // 2. Comprobar duplicados
        if (userRepository.existsByEmail(email)) {
            throw IllegalArgumentException("El email ya está registrado")
        }
        if (userRepository.existsByUsername(username)) {
            throw UsernameAlreadyExistsException(username)
        }

        // 3. Crear usuario
        val newUser =
            User(
                id = UUID.randomUUID(),
                email = email,
                username = username,
                passwordHash = passwordEncoder.encode(password)!!,
                description = null,
                avatarNum = null,
            )
        return userRepository.save(newUser)
    }

    fun login(
        identifier: String,
        password: String,
    ): AuthResult {
        // Si contiene @ es email, si no, es username
        val user =
            if (identifier.contains("@")) {
                userRepository.findByEmail(identifier)
            } else {
                userRepository.findByUsername(identifier)
            } ?: throw InvalidCredentialsException()

        if (!passwordEncoder.matches(password, user.passwordHash)) {
            throw InvalidCredentialsException()
        }

        return issueTokens(user)
    }

    @Transactional
    fun refresh(refreshToken: String): AuthResult {
        val userId = refreshTokenService.validateAndConsume(refreshToken)
        val user = userRepository.findById(userId) ?: throw UserNotFoundException(userId)
        return issueTokens(user)
    }

    @Transactional
    fun logout(refreshToken: String) {
        refreshTokenService.revoke(refreshToken)
    }

    private fun issueTokens(user: User): AuthResult {
        val access = jwtService.generateAccessToken(user.id, user.username)
        val refresh = refreshTokenService.generate(user.id)
        return AuthResult(user, access, refresh)
    }
}
