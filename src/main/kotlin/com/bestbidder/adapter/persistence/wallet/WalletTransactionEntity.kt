package com.bestbidder.adapter.persistence.wallet

import com.bestbidder.domain.wallet.TransactionType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "wallet_transaction")
@Suppress("LongParameterList")
class WalletTransactionEntity(
    @Id val id: UUID,
    @Column(name = "user_id", nullable = false)
    val userId: UUID,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: TransactionType,
    @Column(nullable = false)
    val amount: Double,
    @Column(name = "balance_after", nullable = false)
    val balanceAfter: Double,
    @Column(name = "reference_id")
    val referenceId: UUID?,
    @Column(nullable = false)
    val description: String,
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime,
)
