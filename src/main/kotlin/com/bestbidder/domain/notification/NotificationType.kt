package com.bestbidder.domain.notification

/**
 * Tipos de notificación push que puede recibir un usuario.
 * El front lo recibe en el campo `type` del payload para decidir a qué pantalla navegar.
 */
enum class NotificationType {
    NEW_FOLLOWER,
    FOLLOWEE_CREATED_AUCTION,
    FAVORITE_STARTED,
    FAVORITE_ENDED,
    OUTBID,
    AUCTION_WON,
    OWN_AUCTION_STARTED,
    OWN_AUCTION_ENDED,
    OWN_AUCTION_NEW_BID,
    TEST,
}
