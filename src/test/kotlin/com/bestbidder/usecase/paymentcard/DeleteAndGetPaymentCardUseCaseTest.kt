package com.bestbidder.usecase.paymentcard

import com.bestbidder.domain.paymentcard.PaymentCard
import com.bestbidder.domain.paymentcard.exceptions.PaymentCardNotFoundException
import com.bestbidder.domain.user.User
import com.bestbidder.usecase.port.UserRepository
import com.bestbidder.usecase.user.GetUserUseCase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.util.UUID

class DeleteAndGetPaymentCardUseCaseTest {
    private val userRepository: UserRepository = mock(UserRepository::class.java)
    private val getUserUseCase: GetUserUseCase = mock(GetUserUseCase::class.java)
    private val deleteUseCase = DeletePaymentCardUseCase(userRepository, getUserUseCase)
    private val getUseCase = GetPaymentCardUseCase(getUserUseCase)

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
        )

    @Test
    @DisplayName("Borrar deja al usuario sin tarjeta")
    fun `should delete card`() {
        `when`(getUserUseCase.getById(userId)).thenReturn(buildUser(card))

        deleteUseCase.execute(userId)

        val captor = ArgumentCaptor.forClass(User::class.java)
        verify(userRepository).save(captor.capture())
        assertNull(captor.value.paymentCard)
    }

    @Test
    fun `should throw on delete when user has no card`() {
        `when`(getUserUseCase.getById(userId)).thenReturn(buildUser(null))

        assertThrows(PaymentCardNotFoundException::class.java) { deleteUseCase.execute(userId) }

        verify(userRepository, never()).save(any(User::class.java))
    }

    @Test
    fun `should return the card`() {
        `when`(getUserUseCase.getById(userId)).thenReturn(buildUser(card))

        assertEquals(card, getUseCase.execute(userId))
    }

    @Test
    fun `should throw on get when user has no card`() {
        `when`(getUserUseCase.getById(userId)).thenReturn(buildUser(null))

        assertThrows(PaymentCardNotFoundException::class.java) { getUseCase.execute(userId) }
    }
}
