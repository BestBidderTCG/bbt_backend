package com.bestbidder.domain.follow

import java.util.UUID

/**
 * Usuario que aparece en una lista de seguidos o de seguidores.
 *
 * @property followedByMe true si quien consulta la lista sigue a este usuario.
 */
data class FollowListItem(
    val id: UUID,
    val username: String,
    val avatarNum: Int?,
    val followedByMe: Boolean,
)
