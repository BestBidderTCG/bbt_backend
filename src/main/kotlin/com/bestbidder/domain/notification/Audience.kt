package com.bestbidder.domain.notification

import java.util.UUID

/**
 * A quién va dirigido un evento SSE.
 */
sealed interface Audience {
    /** Todos los clientes conectados. */
    data object Everyone : Audience

    /** Solo las conexiones de estos usuarios (un usuario puede tener varios dispositivos conectados). */
    data class Users(val userIds: Set<UUID>) : Audience {
        constructor(userId: UUID) : this(setOf(userId))
    }
}
