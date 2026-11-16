package com.bestbidder.adapter.web.user.dto

import com.bestbidder.domain.user.User
import com.bestbidder.usecase.user.PurgeDeletedAccountsUseCase
import java.time.Duration
import java.time.Instant

enum class DeletionStatus { ACTIVE, PENDING_DELETION, DELETED }

data class DeletionStatusResponse(
    val status: DeletionStatus,
    val deletionRequestedAt: Instant?,
    val scheduledPurgeAt: Instant?,
) {
    companion object {
        fun from(user: User): DeletionStatusResponse {
            val status =
                when {
                    user.deletedAt != null -> DeletionStatus.DELETED
                    user.deletionRequestedAt != null -> DeletionStatus.PENDING_DELETION
                    else -> DeletionStatus.ACTIVE
                }
            return DeletionStatusResponse(
                status = status,
                deletionRequestedAt = user.deletionRequestedAt,
                scheduledPurgeAt =
                    user.deletionRequestedAt?.plus(
                        Duration.ofDays(PurgeDeletedAccountsUseCase.GRACE_DAYS),
                    ),
            )
        }
    }
}
