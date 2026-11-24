package com.bestbidder.adapter.web.device

import com.bestbidder.adapter.web.device.dto.RegisterDeviceRequest
import com.bestbidder.infrastructure.security.AuthenticatedUser
import com.bestbidder.usecase.device.ManageDeviceTokenUseCase
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * Registro de dispositivos para notificaciones push (FCM).
 */
@RestController
@RequestMapping("/api/devices")
class DeviceController(
    private val manageDeviceTokenUseCase: ManageDeviceTokenUseCase,
) {
    /**
     * Registra o refresca el token FCM del dispositivo. Llamar tras el login
     * y cada vez que Firebase renueve el token. Es idempotente.
     */
    @PostMapping
    fun register(
        @AuthenticationPrincipal principal: AuthenticatedUser,
        @Valid @RequestBody request: RegisterDeviceRequest,
    ): ResponseEntity<Void> {
        manageDeviceTokenUseCase.register(principal.id, request.token, request.platform)
        return ResponseEntity.noContent().build()
    }

    /**
     * Elimina el token del dispositivo. Llamar en el logout para dejar de recibir
     * las notificaciones de este usuario en ese móvil. Es idempotente.
     */
    @DeleteMapping
    fun unregister(
        @AuthenticationPrincipal principal: AuthenticatedUser,
        @RequestParam token: String,
    ): ResponseEntity<Void> {
        manageDeviceTokenUseCase.unregister(principal.id, token)
        return ResponseEntity.noContent().build()
    }
}
