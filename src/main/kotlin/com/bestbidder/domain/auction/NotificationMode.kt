package com.bestbidder.domain.auction

/**
 * Nivel de notificaciones que un usuario quiere recibir de una subasta favorita.
 *
 * - [NONE]: ninguna notificación.
 * - [BASIC]: inicio y fin de la subasta.
 * - [ALL]: inicio, fin y pujas.
 */
enum class NotificationMode {
    NONE,
    BASIC,
    ALL,
    ;

    companion object {
        val DEFAULT = BASIC
    }
}
