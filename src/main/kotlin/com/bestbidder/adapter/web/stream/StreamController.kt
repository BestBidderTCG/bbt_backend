package com.bestbidder.adapter.web.stream

import com.bestbidder.infrastructure.notification.SseEventAdapter
import com.bestbidder.infrastructure.security.AuthenticatedUser
import org.springframework.http.CacheControl
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

/**
 * Canal de eventos en tiempo real (SSE) del usuario autenticado.
 */
@RestController
@RequestMapping("/api")
class StreamController(
    private val sseEventAdapter: SseEventAdapter,
) {
    /**
     * Abre el stream SSE. Requiere `Authorization: Bearer <token>` (el token solo se valida al conectar).
     * Recibe los eventos para todos más los dirigidos solo a este usuario.
     */
    @GetMapping("/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun stream(
        @AuthenticationPrincipal principal: AuthenticatedUser,
    ): ResponseEntity<SseEmitter> =
        ResponseEntity.ok()
            .cacheControl(CacheControl.noCache())
            // Evita que nginx y similares acumulen la respuesta en buffer y los eventos lleguen tarde
            .header("X-Accel-Buffering", "no")
            .body(sseEventAdapter.subscribe(principal.id))
}
