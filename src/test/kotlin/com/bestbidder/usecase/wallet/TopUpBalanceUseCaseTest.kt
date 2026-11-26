package com.bestbidder.usecase.wallet

import com.bestbidder.domain.paymentcard.PaymentCard
import com.bestbidder.domain.paymentcard.exceptions.PaymentCardNotFoundException
import com.bestbidder.domain.user.User
import com.bestbidder.domain.wallet.TransactionType
import com.bestbidder.domain.wallet.WalletTransaction
import com.bestbidder.usecase.port.UserRepository
import com.bestbidder.usecase.port.WalletTransactionRepository
import com.bestbidder.usecase.user.GetUserUseCase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyDouble
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.util.UUID

class TopUpBalanceUseCaseTest {
    private val getUserUseCase: GetUserUseCase = mock(GetUserUseCase::class.java)
    private val userRepository: UserRepository = mock(UserRepository::class.java)
    private val walletTransactionRepository: WalletTransactionRepository =
        mock(WalletTransactionRepository::class.java)
    private val useCase = TopUpBalanceUseCase(getUserUseCase, userRepository, walletTransactionRepository)

    private val userId = UUID.randomUUID()
    private val card = PaymentCard(holderName = "Paula", last4 = "1234", expiryMonth = 12, expiryYear = 2099)

    private fun buildUser(card: PaymentCard?) =
        User(
            id = userId,
            email = "paula@test.com",
            passwordHash = "hash",
            username = "paula",
            description = null,
            avatarNum = 1,
            paymentCard = card,
            balance = 100.0,
        )

    private fun assertNothingChanged() {
        verify(userRepository, never()).addBalance(any(UUID::class.java), anyDouble())
        verify(walletTransactionRepository, never()).save(any(WalletTransaction::class.java))
    }

    @Test
    @DisplayName("Suma el saldo y guarda un movimiento TOP_UP con el saldo resultante")
    fun `should top up balance and record transaction`() {
        `when`(getUserUseCase.getById(userId)).thenReturn(buildUser(card))
        `when`(userRepository.addBalance(userId, 50.0)).thenReturn(150.0)
        `when`(walletTransactionRepository.save(any(WalletTransaction::class.java)))
            .thenAnswer { it.arguments[0] as WalletTransaction }

        val result = useCase.execute(TopUpBalanceCommand(userId, 50.0))

        verify(userRepository).addBalance(userId, 50.0)

        val captor = ArgumentCaptor.forClass(WalletTransaction::class.java)
        verify(walletTransactionRepository).save(captor.capture())
        val saved = captor.value
        assertEquals(TransactionType.TOP_UP, saved.type)
        assertEquals(userId, saved.userId)
        assertEquals(50.0, saved.amount)
        assertEquals(150.0, saved.balanceAfter)
        assertTrue(saved.description.contains("1234"))
        assertEquals(saved, result)
    }

    @Test
    @DisplayName("No permite recargar sin tarjeta registrada")
    fun `should throw when user has no card`() {
        `when`(getUserUseCase.getById(userId)).thenReturn(buildUser(null))

        assertThrows(PaymentCardNotFoundException::class.java) {
            useCase.execute(TopUpBalanceCommand(userId, 50.0))
        }

        assertNothingChanged()
    }

    @Test
    fun `should reject zero amount`() {
        assertThrows(IllegalArgumentException::class.java) { useCase.execute(TopUpBalanceCommand(userId, 0.0)) }
        assertNothingChanged()
    }

    @Test
    fun `should reject negative amount`() {
        assertThrows(IllegalArgumentException::class.java) { useCase.execute(TopUpBalanceCommand(userId, -10.0)) }
        assertNothingChanged()
    }

    @Test
    fun `should reject amount above the maximum`() {
        val tooMuch = TopUpBalanceUseCase.MAX_TOP_UP + 1
        assertThrows(IllegalArgumentException::class.java) { useCase.execute(TopUpBalanceCommand(userId, tooMuch)) }
        assertNothingChanged()
    }
}
