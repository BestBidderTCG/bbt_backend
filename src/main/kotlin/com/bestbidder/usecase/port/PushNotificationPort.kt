package com.bestbidder.usecase.port

import com.bestbidder.domain.notification.PushMessage
import java.util.UUID

/**
 * Puerto de salida para enviar notificaciones push a usuarios concretos.
 *
 * A diferencia de [RealtimeEventPort] (SSE, solo a los clientes conectados en ese momento),
 * este puerto entrega el mensaje únicamente a los dispositivos de los usuarios indicados,
 * incluso con la app cerrada.
 */
interface PushNotificationPort {
    /**
     * Envía [message] a todos los dispositivos registrados de [userIds].
     * No falla si algún usuario no tiene dispositivos. Si hay una transacción activa,
     * el envío se pospone hasta que se confirme el commit.
     */
    fun send(
        userIds: Collection<UUID>,
        message: PushMessage,
    )
}
