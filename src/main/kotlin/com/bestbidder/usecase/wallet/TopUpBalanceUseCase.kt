package com.bestbidder.usecase.wallet

import com.bestbidder.domain.paymentcard.exceptions.PaymentCardNotFoundException
import com.bestbidder.domain.wallet.TransactionType
import com.bestbidder.domain.wallet.WalletTransaction
import com.bestbidder.usecase.port.UserRepository
import com.bestbidder.usecase.port.WalletTransactionRepository
import com.bestbidder.usecase.user.GetUserUseCase
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

data class TopUpBalanceCommand(
    val userId: UUID,
    val amount: Double,
)

@Service
class TopUpBalanceUseCase(
    private val getUserUseCase: GetUserUseCase,
    private val userRepository: UserRepository,
    private val walletTransactionRepository: WalletTransactionRepository,
) {
    @Transactional
    fun execute(command: TopUpBalanceCommand): WalletTransaction {
        require(command.amount > 0.0) { "El importe de la recarga debe ser mayor que 0" }
        require(command.amount <= MAX_TOP_UP) { "El importe máximo por recarga es de $MAX_TOP_UP €" }

        val user = getUserUseCase.getById(command.userId)
        val card = user.paymentCard ?: throw PaymentCardNotFoundException(command.userId)

        // Cobro simulado: aquí iría la llamada a la pasarela de pago
        val newBalance = userRepository.addBalance(command.userId, command.amount)

        return walletTransactionRepository.save(
            WalletTransaction(
                userId = command.userId,
                type = TransactionType.TOP_UP,
                amount = command.amount,
                balanceAfter = newBalance,
                description = "Recarga con tarjeta ${card.masked()}",
            ),
        )
    }

    companion object {
        const val MAX_TOP_UP = 1000.0
    }
}
