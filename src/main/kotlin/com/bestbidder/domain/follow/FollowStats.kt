package com.bestbidder.domain.follow

/**
 * Contadores sociales del perfil de un usuario.
 *
 * @property following true si quien consulta sigue a este usuario.
 */
data class FollowStats(
    val followersCount: Long,
    val followingCount: Long,
    val following: Boolean,
)
