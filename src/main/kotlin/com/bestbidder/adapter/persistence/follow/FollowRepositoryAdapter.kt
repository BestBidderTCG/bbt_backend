package com.bestbidder.adapter.persistence.follow

import com.bestbidder.domain.auction.AuctionListItem
import com.bestbidder.domain.follow.Follow
import com.bestbidder.domain.follow.FollowListItem
import com.bestbidder.usecase.port.FollowRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Component
class FollowRepositoryAdapter(
    private val jpaFollowRepository: JpaFollowRepository,
) : FollowRepository {
    @Transactional
    override fun follow(follow: Follow): Boolean =
        jpaFollowRepository.insertIfAbsent(
            id = follow.id,
            followerId = follow.followerId,
            followeeId = follow.followeeId,
            createdAt = follow.createdAt,
        ) > 0

    @Transactional
    override fun unfollow(
        followerId: UUID,
        followeeId: UUID,
    ): Boolean = jpaFollowRepository.deleteByPair(followerId, followeeId) > 0

    @Transactional(readOnly = true)
    override fun isFollowing(
        followerId: UUID,
        followeeId: UUID,
    ): Boolean = jpaFollowRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId)

    @Transactional(readOnly = true)
    override fun getFollowing(
        userId: UUID,
        viewerId: UUID,
        page: Int,
        size: Int,
    ): List<FollowListItem> =
        jpaFollowRepository
            .findFollowing(userId, viewerId, PageRequest.of(page, size))
            .map { it.toDomain() }

    @Transactional(readOnly = true)
    override fun getFollowers(
        userId: UUID,
        viewerId: UUID,
        page: Int,
        size: Int,
    ): List<FollowListItem> =
        jpaFollowRepository
            .findFollowers(userId, viewerId, PageRequest.of(page, size))
            .map { it.toDomain() }

    @Transactional(readOnly = true)
    override fun countFollowing(userId: UUID): Long = jpaFollowRepository.countFollowing(userId)

    @Transactional(readOnly = true)
    override fun countFollowers(userId: UUID): Long = jpaFollowRepository.countFollowers(userId)

    @Transactional(readOnly = true)
    override fun getFollowingAuctions(userId: UUID): List<AuctionListItem> {
        val now = Instant.now()
        return jpaFollowRepository
            .findFollowingAuctions(userId, now)
            .map { it.toSummary(now) }
    }
}
