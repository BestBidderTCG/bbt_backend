package com.bestbidder.adapter.web.follow.dto

import com.bestbidder.domain.follow.FollowListItem
import java.util.UUID

data class FollowListItemResponse(
    val id: UUID,
    val username: String,
    val avatarNum: Int?,
    val followedByMe: Boolean,
) {
    companion object {
        fun fromDomain(user: FollowListItem) =
            FollowListItemResponse(
                id = user.id,
                username = user.username,
                avatarNum = user.avatarNum,
                followedByMe = user.followedByMe,
            )
    }
}
