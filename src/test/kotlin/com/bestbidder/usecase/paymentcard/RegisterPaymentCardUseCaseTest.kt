package com.bestbidder.usecase.paymentcard

import com.bestbidder.domain.paymentcard.PaymentCard
import com.bestbidder.domain.paymentcard.exceptions.InvalidPaymentCardException
import com.bestbidder.domain.paymentcard.exceptions.PaymentCardAlreadyExistsException
import com.bestbidder.domain.user.User
import com.bestbidder.usecase.port.UserRepository
import com.bestbidder.usecase.user.GetUserUseCase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.time.YearMonth
import java.util.UUID

class RegisterPaymentCardUseCaseTest {
    private val userRepository: UserRepository = mock(UserRepository::class.java)
    private val getUserUseCase: GetUserUseCase = mock(GetUserUseCase::class.java)
    private val useCase = RegisterPaymentCardUseCase(userRepository, getUserUseCase)

    private val userId = UUID.randomUUID()
    private val nextYear = YearMonth.now().year + 1

    private fun buildUser(card: PaymentCard? = null) =
        User(
            id = userId,
            email = "paula@test.com",
            passwordHash = "hash",
            username = "paula",
            description = null,
            avatarNum = 1,
            paymentCard = card,
        )

    private fun command(cardNumber: String = "1234123412341234") =
        RegisterPaymentCardCommand(
            userId = userId,
            cardNumber = cardNumber,
            holderName = "  Paula  ",
            expiryMonth = 12,
            expiryYear = nextYear,
            cvv = "123",
        )

    @Test
    @DisplayName("Guarda solo los últimos 4 dígitos y nunca el número completo")
    fun `should register card keeping only last4`() {
        `when`(getUserUseCase.getById(userId)).thenReturn(buildUser())

        val result = useCase.execute(command())

        assertEquals("1234", result.last4)
        assertEquals("Paula", result.holderName)

        val captor = ArgumentCaptor.forClass(User::class.java)
        verify(userRepository).save(captor.capture())
        assertNotNull(captor.value.paymentCard)
        assertEquals(result, captor.value.paymentCard)
    }

    @Test
    @DisplayName("Lanza excepción si el usuario ya tiene tarjeta")
    fun `should throw when user already has a card`() {
        val existing = PaymentCard(holderName = "Paula", last4 = "9999", expiryMonth = 1, expiryYear = nextYear)
        `when`(getUserUseCase.getById(userId)).thenReturn(buildUser(existing))

        assertThrows(PaymentCardAlreadyExistsException::class.java) { useCase.execute(command()) }

        verify(userRepository, never()).save(any(User::class.java))
    }

    @Test
    @DisplayName("Lanza excepción y no guarda si los datos no son válidos")
    fun `should throw and not save when card data is invalid`() {
        `when`(getUserUseCase.getById(userId)).thenReturn(buildUser())

        assertThrows(InvalidPaymentCardException::class.java) { useCase.execute(command(cardNumber = "123")) }

        verify(userRepository, never()).save(any(User::class.java))
    }
}
