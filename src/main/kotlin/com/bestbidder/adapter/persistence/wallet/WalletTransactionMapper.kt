package com.bestbidder.adapter.persistence.wallet

import com.bestbidder.domain.wallet.WalletTransaction
import com.bestbidder.usecase.port.WalletTransactionRepository
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class WalletTransactionMapper(
    private val jpaRepository: JpaWalletTransactionRepository,
) : WalletTransactionRepository {
    override fun save(transaction: WalletTransaction): WalletTransaction {
        val saved = jpaRepository.save(transaction.toEntity())
        return saved.toDomain()
    }

    override fun findByUserId(userId: UUID): List<WalletTransaction> =
        jpaRepository.findByUserIdOrderByCreatedAtDesc(userId).map { it.toDomain() }
}

private fun WalletTransaction.toEntity() =
    WalletTransactionEntity(
        id = id,
        userId = userId,
        type = type,
        amount = amount,
        balanceAfter = balanceAfter,
        referenceId = referenceId,
        description = description,
        createdAt = createdAt,
    )

private fun WalletTransactionEntity.toDomain() =
    WalletTransaction(
        id = id,
        userId = userId,
        type = type,
        amount = amount,
        balanceAfter = balanceAfter,
        referenceId = referenceId,
        description = description,
        createdAt = createdAt,
    )
