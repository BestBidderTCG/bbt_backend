package com.bestbidder.usecase.follow

import com.bestbidder.domain.auction.AuctionListItem
import com.bestbidder.domain.auction.AuctionStatus
import com.bestbidder.domain.follow.FollowListItem
import com.bestbidder.domain.follow.FollowStats
import com.bestbidder.domain.user.exceptions.UserNotFoundException
import com.bestbidder.usecase.port.FollowRepository
import com.bestbidder.usecase.port.UserRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class GetFollowUseCase(
    private val followRepository: FollowRepository,
    private val userRepository: UserRepository,
) {
    companion object {
        const val MAX_PAGE_SIZE = 50
    }

    /**
     * Usuarios a los que sigue [userId]. [viewerId] es quien consulta.
     */
    fun getFollowing(
        userId: UUID,
        viewerId: UUID,
        page: Int,
        size: Int,
    ): List<FollowListItem> {
        ensureUserExists(userId)
        return followRepository.getFollowing(userId, viewerId, page.coerceAtLeast(0), size.coerceIn(1, MAX_PAGE_SIZE))
    }

    /**
     * Usuarios que siguen a [userId]. [viewerId] es quien consulta.
     */
    fun getFollowers(
        userId: UUID,
        viewerId: UUID,
        page: Int,
        size: Int,
    ): List<FollowListItem> {
        ensureUserExists(userId)
        return followRepository.getFollowers(userId, viewerId, page.coerceAtLeast(0), size.coerceIn(1, MAX_PAGE_SIZE))
    }

    /**
     * Contadores del perfil de [userId] y si [viewerId] lo sigue.
     */
    fun getStats(
        userId: UUID,
        viewerId: UUID,
    ): FollowStats {
        ensureUserExists(userId)
        return FollowStats(
            followersCount = followRepository.countFollowers(userId),
            followingCount = followRepository.countFollowing(userId),
            following = followRepository.isFollowing(viewerId, userId),
        )
    }

    /**
     * Subastas activas o próximas de los usuarios que sigue [userId].
     *
     * @param status filtro opcional: solo UPCOMING o IN_PROGRESS. Si es null devuelve ambas.
     */
    fun getFollowingAuctions(
        userId: UUID,
        status: AuctionStatus? = null,
    ): List<AuctionListItem> {
        require(status == null || status == AuctionStatus.UPCOMING || status == AuctionStatus.IN_PROGRESS) {
            "El estado solo puede ser UPCOMING o IN_PROGRESS"
        }
        val auctions = followRepository.getFollowingAuctions(userId)
        return if (status == null) auctions else auctions.filter { it.status == status }
    }

    private fun ensureUserExists(userId: UUID) {
        val user = userRepository.findById(userId)
        if (user == null || user.isDeleted) throw UserNotFoundException(userId)
    }
}
