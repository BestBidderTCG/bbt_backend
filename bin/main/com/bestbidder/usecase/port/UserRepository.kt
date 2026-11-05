package com.bestbidder.usecase.port

import com.bestbidder.domain.user.User
import java.util.UUID

/**
 * Puerto de persistencia para la gestión de usuarios e identidades del sistema.
 */
interface UserRepository {
    /**
     * Busca un usuario por su nombre de usuario.
     */
    fun findByUsername(username: String): User?

    /**
     * Busca un usuario por su identificador único.
     */
    fun findById(id: UUID): User?

    /**
     * Comprueba si existe algún usuario registrado con el nombre de usuario indicando.
     */
    fun existsByUsername(username: String): Boolean

    /**
     * Guarda o actualiza los datos de un usuario en el sistema.
     */
    fun save(user: User): User

    /**
     * Comprueba si un correo electrónico ya está registrado.
     */
    fun existsByEmail(email: String): Boolean

    /**
     * Busca un usuario asociado a una dirección de correo electrónico.
     */
    fun findByEmail(email: String): User?
}
