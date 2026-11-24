package com.bestbidder.domain.notification

/**
 * Mensaje push independiente del proveedor (FCM u otro).
 *
 * @property data Pares clave/valor (siempre String) para el deep link: `auctionId`, `userId`...
 */
data class PushMessage(
    val type: NotificationType,
    val title: String,
    val body: String,
    val data: Map<String, String> = emptyMap(),
)
