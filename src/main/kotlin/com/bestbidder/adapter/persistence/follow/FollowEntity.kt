package com.bestbidder.adapter.persistence.follow

import com.bestbidder.adapter.persistence.user.UserEntity
import com.bestbidder.domain.follow.Follow
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "follow",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_follow_follower_followee",
            columnNames = ["follower_id", "followee_id"],
        ),
    ],
    indexes = [
        Index(name = "idx_follow_followee", columnList = "followee_id"),
    ],
)
class FollowEntity(
    @Id
    val id: UUID = UUID.randomUUID(),
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", nullable = false)
    val follower: UserEntity,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "followee_id", nullable = false)
    val followee: UserEntity,
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),
) {
    fun toDomain(): Follow =
        Follow(
            id = id,
            followerId = follower.id,
            followeeId = followee.id,
            createdAt = createdAt,
        )

    companion object {
        fun fromDomain(
            domain: Follow,
            followerEntity: UserEntity,
            followeeEntity: UserEntity,
        ): FollowEntity =
            FollowEntity(
                id = domain.id,
                follower = followerEntity,
                followee = followeeEntity,
                createdAt = domain.createdAt,
            )
    }
}
