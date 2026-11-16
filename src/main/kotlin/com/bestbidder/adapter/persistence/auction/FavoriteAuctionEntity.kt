package com.bestbidder.adapter.persistence.auction

import com.bestbidder.adapter.persistence.user.UserEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "favorite_auction",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_favorite_user_auction",
            columnNames = ["user_id", "auction_id"],
        ),
    ],
)
class FavoriteAuctionEntity(
    @Id
    val id: UUID = UUID.randomUUID(),
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: UserEntity,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id", nullable = false)
    val auction: AuctionEntity,
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),
)
