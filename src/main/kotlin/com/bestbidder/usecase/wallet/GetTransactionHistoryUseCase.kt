package com.bestbidder.usecase.wallet

import com.bestbidder.domain.wallet.WalletTransaction
import com.bestbidder.usecase.port.WalletTransactionRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class GetTransactionHistoryUseCase(
    private val walletTransactionRepository: WalletTransactionRepository,
) {
    fun execute(userId: UUID): List<WalletTransaction> = walletTransactionRepository.findByUserId(userId)
}
