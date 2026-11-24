package com.bestbidder.adapter.web.device

import com.bestbidder.domain.notification.PushMessages
import com.bestbidder.infrastructure.security.AuthenticatedUser
import com.bestbidder.usecase.port.PushNotificationPort
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Endpoint SOLO para desarrollo: manda una push de prueba a los dispositivos del propio usuario.
 * Se activa con `bestbidder.push.test-endpoint-enabled=true` (PUSH_TEST_ENDPOINT=true).
 */
@RestController
@RequestMapping("/api/devices")
@ConditionalOnProperty(name = ["bestbidder.push.test-endpoint-enabled"], havingValue = "true")
class PushTestController(
    private val pushNotificationPort: PushNotificationPort,
) {
    @PostMapping("/test-push")
    fun sendTestPush(
        @AuthenticationPrincipal principal: AuthenticatedUser,
    ): ResponseEntity<Void> {
        pushNotificationPort.send(listOf(principal.id), PushMessages.test())
        return ResponseEntity.accepted().build()
    }
}
