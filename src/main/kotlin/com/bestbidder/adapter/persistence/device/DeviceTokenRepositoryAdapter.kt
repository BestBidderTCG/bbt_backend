package com.bestbidder.adapter.persistence.device

import com.bestbidder.domain.device.DevicePlatform
import com.bestbidder.usecase.port.DeviceTokenRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Repository
class DeviceTokenRepositoryAdapter(
    private val jpaDeviceTokenRepository: JpaDeviceTokenRepository,
) : DeviceTokenRepository {
    @Transactional
    override fun register(
        userId: UUID,
        token: String,
        platform: DevicePlatform,
    ) {
        val existing = jpaDeviceTokenRepository.findByToken(token)
        if (existing != null) {
            existing.userId = userId
            existing.platform = platform
            existing.lastSeenAt = Instant.now()
            jpaDeviceTokenRepository.save(existing)
        } else {
            jpaDeviceTokenRepository.save(
                DeviceTokenEntity(userId = userId, token = token, platform = platform),
            )
        }
    }

    @Transactional
    override fun remove(
        userId: UUID,
        token: String,
    ): Boolean = jpaDeviceTokenRepository.deleteByUserIdAndToken(userId, token) > 0

    @Transactional
    override fun removeByToken(token: String) {
        jpaDeviceTokenRepository.deleteByToken(token)
    }

    override fun findTokensByUserIds(userIds: Collection<UUID>): List<String> {
        if (userIds.isEmpty()) return emptyList()
        return jpaDeviceTokenRepository.findTokensByUserIds(userIds)
    }
}
