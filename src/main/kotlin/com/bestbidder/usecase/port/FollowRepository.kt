package com.bestbidder.usecase.port

import com.bestbidder.domain.auction.AuctionListItem
import com.bestbidder.domain.follow.Follow
import com.bestbidder.domain.follow.FollowListItem
import java.util.UUID

/**
 * Puerto de persistencia para las relaciones de seguimiento entre usuarios.
 */
interface FollowRepository {
    /**
     * Registra que un usuario sigue a otro.
     *
     * @return true si la relación se ha creado, false si ya existía.
     */
    fun follow(follow: Follow): Boolean

    /**
     * Elimina la relación de seguimiento.
     *
     * @return true si existía y se ha eliminado, false si no seguía al usuario.
     */
    fun unfollow(
        followerId: UUID,
        followeeId: UUID,
    ): Boolean

    /**
     * Comprueba si [followerId] sigue a [followeeId].
     */
    fun isFollowing(
        followerId: UUID,
        followeeId: UUID,
    ): Boolean

    /**
     * Usuarios a los que sigue [userId], del más reciente al más antiguo.
     * [viewerId] es quien consulta (para calcular `followedByMe`).
     */
    fun getFollowing(
        userId: UUID,
        viewerId: UUID,
        page: Int,
        size: Int,
    ): List<FollowListItem>

    /**
     * Usuarios que siguen a [userId], del más reciente al más antiguo.
     */
    fun getFollowers(
        userId: UUID,
        viewerId: UUID,
        page: Int,
        size: Int,
    ): List<FollowListItem>

    fun countFollowing(userId: UUID): Long

    fun countFollowers(userId: UUID): Long

    /**
     * Subastas activas o próximas creadas por los usuarios a los que sigue [userId],
     * ordenadas por fecha de finalización.
     */
    fun getFollowingAuctions(userId: UUID): List<AuctionListItem>
}
