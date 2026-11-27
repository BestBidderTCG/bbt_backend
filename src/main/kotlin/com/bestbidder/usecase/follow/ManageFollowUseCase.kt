package com.bestbidder.usecase.follow

import com.bestbidder.domain.follow.Follow
import com.bestbidder.domain.notification.Audience
import com.bestbidder.domain.notification.PushMessages
import com.bestbidder.domain.notification.SseEvents
import com.bestbidder.domain.user.exceptions.UserNotFoundException
import com.bestbidder.usecase.port.FollowRepository
import com.bestbidder.usecase.port.PushNotificationPort
import com.bestbidder.usecase.port.RealtimeEventPort
import com.bestbidder.usecase.port.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ManageFollowUseCase(
    private val followRepository: FollowRepository,
    private val userRepository: UserRepository,
    private val pushNotificationPort: PushNotificationPort,
    private val realtimeEventPort: RealtimeEventPort,
) {
    /**
     * Hace que [followerId] siga a [followeeId]. Es idempotente.
     * Si el follow es nuevo, avisa al seguido por push y por SSE (FOLLOWERS_CHANGED).
     *
     * @return true si el follow es nuevo, false si ya existía.
     */
    @Transactional
    fun follow(
        followerId: UUID,
        followeeId: UUID,
    ): Boolean {
        require(followerId != followeeId) { "No puedes seguirte a ti mismo" }

        val followee = userRepository.findById(followeeId)
        if (followee == null || followee.isDeleted) throw UserNotFoundException(followeeId)

        val created =
            followRepository.follow(
                Follow(followerId = followerId, followeeId = followeeId),
            )

        if (created) {
            publishFollowersChanged(followeeId, followerId, followed = true)
            userRepository.findById(followerId)?.let { follower ->
                pushNotificationPort.send(
                    listOf(followeeId),
                    PushMessages.newFollower(followerId, follower.username),
                )
            }
        }
        return created
    }

    /**
     * Deja de seguir al usuario. Es idempotente.
     *
     * @return true si se ha eliminado la relación, false si no existía.
     */
    @Transactional
    fun unfollow(
        followerId: UUID,
        followeeId: UUID,
    ): Boolean {
        val removed = followRepository.unfollow(followerId, followeeId)
        if (removed) publishFollowersChanged(followeeId, followerId, followed = false)
        return removed
    }

    private fun publishFollowersChanged(
        followeeId: UUID,
        followerId: UUID,
        followed: Boolean,
    ) {
        realtimeEventPort.publish(
            SseEvents.followersChanged(followeeId, followerId, followed, followRepository.countFollowers(followeeId)),
            Audience.Users(followeeId),
        )
    }
}
