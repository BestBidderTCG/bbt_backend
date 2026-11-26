package com.bestbidder.usecase.wallet

import com.bestbidder.domain.wallet.TransactionType
import com.bestbidder.domain.wallet.WalletTransaction
import com.bestbidder.usecase.port.WalletTransactionRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.util.UUID

class GetTransactionHistoryUseCaseTest {
    private val repository: WalletTransactionRepository = mock(WalletTransactionRepository::class.java)
    private val useCase = GetTransactionHistoryUseCase(repository)

    @Test
    fun `should return the user's transactions`() {
        val userId = UUID.randomUUID()
        val transactions =
            listOf(
                WalletTransaction(
                    userId = userId,
                    type = TransactionType.TOP_UP,
                    amount = 50.0,
                    balanceAfter = 50.0,
                    description = "Recarga",
                ),
            )
        `when`(repository.findByUserId(userId)).thenReturn(transactions)

        assertEquals(transactions, useCase.execute(userId))
    }
}
