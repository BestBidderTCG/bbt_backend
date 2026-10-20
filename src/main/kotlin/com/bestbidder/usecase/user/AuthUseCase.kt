package com.bestbidder.usecase.user

import com.bestbidder.domain.user.User
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
    private val passwordEncoder: PasswordEncoder
) {
    fun register(username: String, password: String): User {
        // 1. Comprobar si ya existe
        if (userRepository.findByUsername(username) != null) {
            throw IllegalArgumentException("El nombre de usuario ya está en uso")
        }
        
        // 2. Crear usuario con contraseña encriptada
        val newUser = User(
            id = UUID.randomUUID(),
            username = username,
            passwordHash = passwordEncoder.encode(password)!!
        )
        
        // 3. Guardar
        return userRepository.save(newUser)
    }

    fun login(username: String, password: String): User {
        // 1. Buscar usuario
        val user = userRepository.findByUsername(username) 
            ?: throw IllegalArgumentException("Usuario o contraseña incorrectos")
        
        // 2. Comprobar contraseña
        if (!passwordEncoder.matches(password, user.passwordHash)) {
            throw IllegalArgumentException("Usuario o contraseña incorrectos")
        }
        
        return user
    }
}