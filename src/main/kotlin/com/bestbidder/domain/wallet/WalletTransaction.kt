package com.bestbidder.domain.wallet

import java.time.LocalDateTime
import java.util.UUID

data class WalletTransaction(
    val id: UUID = UUID.randomUUID(),
    val userId: UUID,
    val type: TransactionType,
    val amount: Double,
    val balanceAfter: Double,
    val referenceId: UUID? = null,
    val description: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
)
