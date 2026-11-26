package com.bestbidder.domain.user

import com.bestbidder.domain.paymentcard.PaymentCard
import java.time.Instant
import java.time.LocalDateTime
import java.util.UUID

data class User(
    val id: UUID,
    val email: String,
    val passwordHash: String,
    val username: String,
    val description: String?,
    val avatarNum: Int?,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val deletionRequestedAt: Instant? = null,
    val deletedAt: Instant? = null,
    val paymentCard: PaymentCard? = null,
    val balance: Double = 0.0,
) {
    val isDeleted: Boolean get() = deletedAt != null
    val isPendingDeletion: Boolean get() = deletionRequestedAt != null && deletedAt == null
}
