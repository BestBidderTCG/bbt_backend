package com.bestbidder.adapter.persistence.wallet

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface JpaWalletTransactionRepository : JpaRepository<WalletTransactionEntity, UUID> {
    fun findByUserIdOrderByCreatedAtDesc(userId: UUID): List<WalletTransactionEntity>
}
