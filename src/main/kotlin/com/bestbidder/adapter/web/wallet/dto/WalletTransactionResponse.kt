package com.bestbidder.adapter.web.wallet.dto

import com.bestbidder.domain.wallet.WalletTransaction
import java.time.LocalDateTime
import java.util.UUID

data class WalletTransactionResponse(
    val id: UUID,
    val type: String,
    val amount: Double,
    val balanceAfter: Double,
    val description: String,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun fromDomain(transaction: WalletTransaction) =
            WalletTransactionResponse(
                id = transaction.id,
                type = transaction.type.name,
                amount = transaction.amount,
                balanceAfter = transaction.balanceAfter,
                description = transaction.description,
                createdAt = transaction.createdAt,
            )
    }
}
