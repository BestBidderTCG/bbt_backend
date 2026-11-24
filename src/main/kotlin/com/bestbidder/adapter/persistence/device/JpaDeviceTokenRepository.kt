package com.bestbidder.adapter.persistence.device

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface JpaDeviceTokenRepository : JpaRepository<DeviceTokenEntity, UUID> {
    fun findByToken(token: String): DeviceTokenEntity?

    fun deleteByUserIdAndToken(
        userId: UUID,
        token: String,
    ): Long

    fun deleteByToken(token: String): Long

    @Query("SELECT d.token FROM DeviceTokenEntity d WHERE d.userId IN :userIds")
    fun findTokensByUserIds(
        @Param("userIds") userIds: Collection<UUID>,
    ): List<String>
}
