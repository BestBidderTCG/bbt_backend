package com.bestbidder.usecase.port

import com.bestbidder.domain.wallet.WalletTransaction
import java.util.UUID

interface WalletTransactionRepository {
    fun save(transaction: WalletTransaction): WalletTransaction

    fun findByUserId(userId: UUID): List<WalletTransaction>
}
