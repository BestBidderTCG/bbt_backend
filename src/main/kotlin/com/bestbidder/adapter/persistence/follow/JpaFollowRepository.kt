package com.bestbidder.adapter.persistence.follow

import com.bestbidder.adapter.persistence.auction.AuctionListItemRow
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID

@Suppress("TooManyFunctions")
interface JpaFollowRepository : JpaRepository<FollowEntity, UUID> {
    fun existsByFollowerIdAndFolloweeId(
        followerId: UUID,
        followeeId: UUID,
    ): Boolean

    /**
     * Inserta el follow ignorando duplicados (seguro ante doble toque o peticiones concurrentes).
     * Devuelve 1 si se ha insertado y 0 si ya existía.
     */
    @Modifying
    @Query(
        nativeQuery = true,
        value = """
        INSERT INTO follow (id, follower_id, followee_id, created_at)
        VALUES (:id, :followerId, :followeeId, :createdAt)
        ON CONFLICT (follower_id, followee_id) DO NOTHING
        """,
    )
    fun insertIfAbsent(
        @Param("id") id: UUID,
        @Param("followerId") followerId: UUID,
        @Param("followeeId") followeeId: UUID,
        @Param("createdAt") createdAt: Instant,
    ): Int

    @Modifying
    @Query(
        """
        DELETE FROM FollowEntity f
        WHERE f.follower.id = :followerId AND f.followee.id = :followeeId
        """,
    )
    fun deleteByPair(
        @Param("followerId") followerId: UUID,
        @Param("followeeId") followeeId: UUID,
    ): Int

    @Query(
        """
        SELECT new com.bestbidder.adapter.persistence.follow.FollowListItemRow(
            u.id, u.username, u.avatarNum,
            CASE WHEN EXISTS (
                SELECT 1 FROM FollowEntity v
                WHERE v.follower.id = :viewerId AND v.followee = u
            ) THEN TRUE ELSE FALSE END
        )
        FROM FollowEntity f
        JOIN f.followee u
        WHERE f.follower.id = :userId AND u.deletedAt IS NULL
        ORDER BY f.createdAt DESC
        """,
    )
    fun findFollowing(
        @Param("userId") userId: UUID,
        @Param("viewerId") viewerId: UUID,
        pageable: Pageable,
    ): List<FollowListItemRow>

    @Query(
        """
        SELECT new com.bestbidder.adapter.persistence.follow.FollowListItemRow(
            u.id, u.username, u.avatarNum,
            CASE WHEN EXISTS (
                SELECT 1 FROM FollowEntity v
                WHERE v.follower.id = :viewerId AND v.followee = u
            ) THEN TRUE ELSE FALSE END
        )
        FROM FollowEntity f
        JOIN f.follower u
        WHERE f.followee.id = :userId AND u.deletedAt IS NULL
        ORDER BY f.createdAt DESC
        """,
    )
    fun findFollowers(
        @Param("userId") userId: UUID,
        @Param("viewerId") viewerId: UUID,
        pageable: Pageable,
    ): List<FollowListItemRow>

    @Query(
        """
        SELECT f.follower.id FROM FollowEntity f
        WHERE f.followee.id = :userId AND f.follower.deletedAt IS NULL
        """,
    )
    fun findFollowerIdsByUserId(
        @Param("userId") userId: UUID,
    ): List<UUID>

    @Query(
        """
        SELECT COUNT(f) FROM FollowEntity f
        WHERE f.follower.id = :userId AND f.followee.deletedAt IS NULL
        """,
    )
    fun countFollowing(
        @Param("userId") userId: UUID,
    ): Long

    @Query(
        """
        SELECT COUNT(f) FROM FollowEntity f
        WHERE f.followee.id = :userId AND f.follower.deletedAt IS NULL
        """,
    )
    fun countFollowers(
        @Param("userId") userId: UUID,
    ): Long

    /**
     * Misma proyección que el listado general de subastas, limitada a creadores seguidos.
     */
    @Query(
        """
        SELECT new com.bestbidder.adapter.persistence.auction.AuctionListItemRow(
            a.id, c.name, c.image, COALESCE(MAX(b.amount), a.minPrize),
            a.startedAt, a.endsAt, c.state, c.category, c.collection, c.num, a.viewCount
        )
        FROM AuctionEntity a
        JOIN a.card c
        JOIN a.creator u
        LEFT JOIN BidEntity b ON b.auction = a
        WHERE a.endsAt > :now
          AND u.deletedAt IS NULL
          AND EXISTS (
              SELECT 1 FROM FollowEntity f
              WHERE f.follower.id = :userId AND f.followee = u
          )
        GROUP BY a.id, c.name, c.image, a.minPrize, a.startedAt, a.endsAt, c.state, c.category, c.collection, c.num
        ORDER BY a.endsAt ASC
        """,
    )
    fun findFollowingAuctions(
        @Param("userId") userId: UUID,
        @Param("now") now: Instant,
    ): List<AuctionListItemRow>
}
