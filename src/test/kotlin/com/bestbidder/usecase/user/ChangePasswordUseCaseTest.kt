package com.bestbidder.usecase.user

import com.bestbidder.domain.user.User
import com.bestbidder.domain.user.exceptions.InvalidCredentialsException
import com.bestbidder.infrastructure.security.RefreshTokenService
import com.bestbidder.usecase.port.UserRepository
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.UUID

class ChangePasswordUseCaseTest {
    private val userRepository: UserRepository = mock(UserRepository::class.java)
    private val passwordEncoder: PasswordEncoder = mock(PasswordEncoder::class.java)
    private val refreshTokenService: RefreshTokenService = mock(RefreshTokenService::class.java)
    private val useCase = ChangePasswordUseCase(userRepository, passwordEncoder, refreshTokenService)

    private val userId = UUID.randomUUID()
    private val mockUser = User(userId, "david@test.com", "hashed_old", "david_85", null, null)

    @Test
    @DisplayName("Debe cambiar la contraseña y revocar los refresh tokens")
    fun `should change password and revoke tokens`() {
        `when`(userRepository.findById(userId)).thenReturn(mockUser)
        `when`(passwordEncoder.matches("secreto85", "hashed_old")).thenReturn(true)
        `when`(passwordEncoder.encode("nuevoSecreto85")).thenReturn("hashed_new")

        useCase.change(userId, "secreto85", "nuevoSecreto85")

        verify(userRepository, times(1)).save(any(User::class.java))
        verify(refreshTokenService, times(1)).revokeAll(userId)
    }

    @Test
    @DisplayName("Debe lanzar excepción si la contraseña actual es incorrecta")
    fun `should throw exception when current password is wrong`() {
        `when`(userRepository.findById(userId)).thenReturn(mockUser)
        `when`(passwordEncoder.matches("wrongPass", "hashed_old")).thenReturn(false)

        assertThrows(InvalidCredentialsException::class.java) {
            useCase.change(userId, "wrongPass", "nuevoSecreto85")
        }
    }
}
