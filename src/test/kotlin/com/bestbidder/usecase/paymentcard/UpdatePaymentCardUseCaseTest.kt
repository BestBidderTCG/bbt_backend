package com.bestbidder.usecase.paymentcard

import com.bestbidder.domain.paymentcard.PaymentCard
import com.bestbidder.domain.paymentcard.exceptions.PaymentCardNotFoundException
import com.bestbidder.domain.user.User
import com.bestbidder.usecase.port.UserRepository
import com.bestbidder.usecase.user.GetUserUseCase
import org.junit.jupiter.api.Assertions.assertEquals
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

class UpdatePaymentCardUseCaseTest {
    private val userRepository: UserRepository = mock(UserRepository::class.java)
    private val getUserUseCase: GetUserUseCase = mock(GetUserUseCase::class.java)
    private val useCase = UpdatePaymentCardUseCase(userRepository, getUserUseCase)

    private val userId = UUID.randomUUID()
    private val nextYear = YearMonth.now().year + 1
    private val existingCard = PaymentCard(holderName = "Paula", last4 = "1111", expiryMonth = 1, expiryYear = nextYear)

    private fun buildUser(card: PaymentCard?) =
        User(
            id = userId,
            email = "paula@test.com",
            passwordHash = "hash",
            username = "paula",
            description = null,
            avatarNum = 1,
            paymentCard = card,
        )

    private val command =
        UpdatePaymentCardCommand(
            userId = userId,
            cardNumber = "5555666677778888",
            holderName = "Paula Nueva",
            expiryMonth = 6,
            expiryYear = nextYear + 1,
            cvv = "321",
        )

    @Test
    @DisplayName("Actualiza los datos conservando el id de la tarjeta")
    fun `should update card keeping the same id`() {
        `when`(getUserUseCase.getById(userId)).thenReturn(buildUser(existingCard))

        val result = useCase.execute(command)

        assertEquals(existingCard.id, result.id)
        assertEquals("8888", result.last4)
        assertEquals("Paula Nueva", result.holderName)

        val captor = ArgumentCaptor.forClass(User::class.java)
        verify(userRepository).save(captor.capture())
        assertEquals(result, captor.value.paymentCard)
    }

    @Test
    @DisplayName("Lanza excepción si el usuario no tiene tarjeta")
    fun `should throw when user has no card`() {
        `when`(getUserUseCase.getById(userId)).thenReturn(buildUser(null))

        assertThrows(PaymentCardNotFoundException::class.java) { useCase.execute(command) }

        verify(userRepository, never()).save(any(User::class.java))
    }
}
