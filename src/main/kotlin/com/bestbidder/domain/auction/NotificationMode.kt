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

        /** Modos que quieren aviso del inicio y el fin de la subasta. */
        val LIFECYCLE = setOf(BASIC, ALL)

        /** Modos que quieren aviso de cada puja. */
        val BIDS = setOf(ALL)
    }
}
