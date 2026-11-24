package com.bestbidder.adapter.web.follow.dto

import com.bestbidder.domain.follow.FollowStats

data class FollowStatsResponse(
    val followersCount: Long,
    val followingCount: Long,
    val following: Boolean,
) {
    companion object {
        fun fromDomain(stats: FollowStats) =
            FollowStatsResponse(
                followersCount = stats.followersCount,
                followingCount = stats.followingCount,
                following = stats.following,
            )
    }
}
