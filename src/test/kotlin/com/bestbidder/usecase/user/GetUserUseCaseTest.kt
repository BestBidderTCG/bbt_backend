package com.bestbidder.usecase.user

import com.bestbidder.domain.user.User
import com.bestbidder.domain.user.exceptions.UserNotFoundException
import com.bestbidder.usecase.port.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.util.UUID

class GetUserUseCaseTest {
    private val userRepository: UserRepository = mock(UserRepository::class.java)
    private val useCase = GetUserUseCase(userRepository)
    private val userId = UUID.randomUUID()

    @Test
    @DisplayName("Debe retornar el usuario si existe")
    fun `should return user when exists`() {
        val user =
            User(
                userId,
                "david@test.com",
                "hashed",
                "david_85",
                null,
                null,
            )
        `when`(userRepository.findById(userId)).thenReturn(user)

        val result = useCase.getById(userId)

        assertEquals(user, result)
    }

    @Test
    @DisplayName("Debe lanzar excepción si el usuario no existe")
    fun `should throw exception when user not found`() {
        `when`(userRepository.findById(userId)).thenReturn(null)

        assertThrows(UserNotFoundException::class.java) {
            useCase.getById(userId)
        }
    }
}
