package com.bestbidder.usecase.port

import java.time.LocalDateTime
import java.util.UUID

/**
 * Puerto de persistencia para el almacenamiento y control de tokens de refresco (JWT).
 */
interface RefreshTokenRepository {
    /**
     * Registra un nuevo token de refresco hasheado y asociado a un usuario.
     */
    fun save(
        userId: UUID,
        tokenHash: String,
        expiresAt: LocalDateTime,
    )

    /**
     * Busca los datos de un token de refresco a partir de su hash.
     */
    fun findByHash(tokenHash: String): StoredRefreshToken?

    /**
     * Elimina un token de refresco mediante su hash (ej. al cerrar sesión).
     */
    fun deleteByHash(tokenHash: String)

    /**
     * Revoca y elimina todos los tokens de refresco activos de un usuario.
     */
    fun deleteAllByUserId(userId: UUID)
}

/**
 * Representa la información almacenada de un token de refresco persistido.
 *
 * @property userId Identificador del usuario propietario del token.
 * @property expiresAt Fecha y hora de expiración del token.
 */
data class StoredRefreshToken(val userId: UUID, val expiresAt: LocalDateTime)
