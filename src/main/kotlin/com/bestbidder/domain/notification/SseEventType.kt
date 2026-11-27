package com.bestbidder.domain.notification

/**
 * Eventos que viajan por el canal SSE (`/api/auction/stream`).
 * El nombre del enum es el nombre del evento SSE (`event: BID_PLACED`).
 *
 * Son avisos "en vivo" para quien tiene la app abierta. Para avisar con la app cerrada
 * se usa [NotificationType] (push FCM).
 */
enum class SseEventType {
    AUCTION_CREATED,
    AUCTION_UPDATED,
    AUCTION_STARTED,
    AUCTION_ENDED,
    BID_PLACED,
    FOLLOWERS_CHANGED,
    BALANCE_CHANGED,
}
