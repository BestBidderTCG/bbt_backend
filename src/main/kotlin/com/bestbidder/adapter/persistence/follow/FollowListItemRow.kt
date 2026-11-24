package com.bestbidder.adapter.persistence.follow

import com.bestbidder.domain.follow.FollowListItem
import java.util.UUID

/**
 * Proyección de las queries de seguidos/seguidores.
 */
data class FollowListItemRow(
    val id: UUID,
    val username: String,
    val avatarNum: Int?,
    val followedByMe: Boolean,
) {
    fun toDomain(): FollowListItem =
        FollowListItem(
            id = id,
            username = username,
            avatarNum = avatarNum,
            followedByMe = followedByMe,
        )
}
