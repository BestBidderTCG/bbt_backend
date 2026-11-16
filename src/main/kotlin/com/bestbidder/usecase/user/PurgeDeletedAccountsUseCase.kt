package com.bestbidder.usecase.user

import com.bestbidder.infrastructure.security.RefreshTokenService
import com.bestbidder.usecase.port.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Instant

@Service
class PurgeDeletedAccountsUseCase(
    private val userRepository: UserRepository,
    private val refreshTokenService: RefreshTokenService,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        const val GRACE_DAYS = 15L
        private const val SUFFIX_LENGTH = 12
    }

    @Suppress("TooGenericExceptionCaught")
    @Transactional
    fun execute(now: Instant) {
        val cutoff = now.minus(Duration.ofDays(GRACE_DAYS))
        val pending = userRepository.findPendingDeletionBefore(cutoff)

        log.info("Purging {} account(s) whose deletion request is older than {} days", pending.size, GRACE_DAYS)

        pending.forEach { user ->
            try {
                val suffix = user.id.toString().replace("-", "").take(SUFFIX_LENGTH)
                userRepository.save(
                    user.copy(
                        email = "deleted_$suffix@anonymized.invalid",
                        username = "deleted_$suffix",
                        passwordHash = "*",
                        description = null,
                        avatarNum = null,
                        deletedAt = now,
                    ),
                )
                refreshTokenService.revokeAll(user.id)
            } catch (e: Exception) {
                log.error("Failed to purge user ${user.id}", e)
            }
        }
    }
}
