package com.bestbidder.usecase.port

import com.bestbidder.domain.notification.Audience
import com.bestbidder.domain.notification.SseEvent

/**
 * Puerto de salida para publicar eventos en tiempo real (SSE) a los clientes conectados.
 *
 * A diferencia de [PushNotificationPort] (FCM, llega con la app cerrada), este canal solo
 * llega a quien tiene la app abierta y conectada en ese momento. No hay reenvío de lo perdido:
 * al reconectar, el cliente debe volver a consultar el estado por REST.
 */
interface RealtimeEventPort {
    /**
     * Publica [event] a [audience]. Si hay una transacción activa, el envío se pospone hasta
     * que se confirme el commit (no se avisa de algo que acaba en rollback ni antes de que
     * el dato sea visible en BD). No falla si no hay nadie conectado.
     */
    fun publish(
        event: SseEvent,
        audience: Audience,
    )
}
