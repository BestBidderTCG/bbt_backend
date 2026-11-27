package com.bestbidder.domain.notification

/**
 * Evento SSE independiente del transporte.
 *
 * @property data Payload que se serializa como JSON en el campo `data` del evento.
 */
data class SseEvent(
    val type: SseEventType,
    val data: Map<String, Any?>,
)
