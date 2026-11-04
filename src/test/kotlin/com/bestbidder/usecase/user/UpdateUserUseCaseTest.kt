package com.bestbidder.usecase.user

import com.bestbidder.domain.user.User
import com.bestbidder.domain.user.exceptions.UserNotFoundException
import com.bestbidder.usecase.port.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.util.UUID

class UpdateUserUseCaseTest {
    private val userRepository: UserRepository = mock(UserRepository::class.java)
    private val useCase = UpdateUserUseCase(userRepository)
    private val userId = UUID.randomUUID()

    private val existingUser =
        User(
            id = userId,
            email = "david@test.com",
            username = "david_85",
            passwordHash = "hashed",
            description = "Hola",
            avatarNum = 1,
        )

    @Test
    @DisplayName("Debe actualizar correctamente la descripción y el avatar")
    fun `should update profile successfully`() {
        `when`(userRepository.findById(userId)).thenReturn(existingUser)
        `when`(userRepository.save(any(User::class.java)))
            .thenAnswer { it.arguments[0] as User }

        val result = useCase.updateProfile(userId, "Nueva descripción", 5)

        assertEquals("Nueva descripción", result.description)
        assertEquals(5, result.avatarNum)
        verify(userRepository, times(1))
            .save(any(User::class.java))
    }

    @Test
    @DisplayName("Debe borrar la descripción si se envía vacía o en blanco")
    fun `should clear description when blank`() {
        `when`(userRepository.findById(userId)).thenReturn(existingUser)
        `when`(userRepository.save(any(User::class.java))).thenAnswer {
            it.arguments[0] as User
        }

        val result = useCase.updateProfile(userId, "   ", null)

        assertNull(result.description)
        assertEquals(1, result.avatarNum)
    }

    @Test
    @DisplayName("Debe lanzar excepción si la descripción supera los 500 caracteres")
    fun `should throw exception when description is too long`() {
        `when`(userRepository.findById(userId)).thenReturn(existingUser)
        val longString = "a".repeat(501)

        assertThrows(IllegalArgumentException::class.java) {
            useCase.updateProfile(userId, longString, null)
        }
    }

    @Test
    @DisplayName("Debe lanzar excepción si el avatar es negativo")
    fun `should throw exception when avatar is negative`() {
        `when`(userRepository.findById(userId)).thenReturn(existingUser)

        assertThrows(IllegalArgumentException::class.java) {
            useCase.updateProfile(userId, null, -1)
        }
    }

    @Test
    @DisplayName("Debe lanzar excepción si el usuario no existe")
    fun `should throw exception when user not found`() {
        `when`(userRepository.findById(userId)).thenReturn(null)

        assertThrows(UserNotFoundException::class.java) {
            useCase.updateProfile(userId, "test", null)
        }
    }
}
