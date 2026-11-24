package com.bestbidder.usecase.device

import com.bestbidder.domain.device.DevicePlatform
import com.bestbidder.usecase.port.DeviceTokenRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ManageDeviceTokenUseCase(
    private val deviceTokenRepository: DeviceTokenRepository,
) {
    private companion object {
        const val MAX_TOKEN_LENGTH = 512
    }

    /**
     * Registra (o refresca) el token del dispositivo del usuario. Es idempotente.
     */
    fun register(
        userId: UUID,
        token: String,
        platform: DevicePlatform,
    ) {
        val cleanToken = token.trim()
        require(cleanToken.isNotEmpty()) { "El token del dispositivo no puede estar vacío" }
        require(cleanToken.length <= MAX_TOKEN_LENGTH) { "El token del dispositivo es demasiado largo" }

        deviceTokenRepository.register(userId, cleanToken, platform)
    }

    /**
     * Elimina el token del usuario (se llama en el logout). Es idempotente.
     */
    fun unregister(
        userId: UUID,
        token: String,
    ): Boolean = deviceTokenRepository.remove(userId, token.trim())
}
