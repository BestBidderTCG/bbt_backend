package com.bestbidder.usecase.port

import com.bestbidder.domain.device.DevicePlatform
import java.util.UUID

/**
 * Puerto de persistencia de los tokens de dispositivo usados para enviar push.
 */
interface DeviceTokenRepository {
    /**
     * Registra el token para el usuario. Si el token ya existía (aunque fuera de otro usuario,
     * p. ej. al cambiar de cuenta en el mismo móvil) se reasigna a [userId].
     */
    fun register(
        userId: UUID,
        token: String,
        platform: DevicePlatform,
    )

    /**
     * Elimina el token de [userId] (logout). @return true si existía.
     */
    fun remove(
        userId: UUID,
        token: String,
    ): Boolean

    /**
     * Elimina el token sin importar el usuario (FCM lo ha marcado como inválido).
     */
    fun removeByToken(token: String)

    /**
     * Tokens de todos los dispositivos de los usuarios indicados.
     */
    fun findTokensByUserIds(userIds: Collection<UUID>): List<String>
}
