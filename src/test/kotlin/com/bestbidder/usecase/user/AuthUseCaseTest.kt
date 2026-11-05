package com.bestbidder.usecase.user

import com.bestbidder.domain.user.User
import com.bestbidder.domain.user.exceptions.InvalidCredentialsException
import com.bestbidder.domain.user.exceptions.UsernameAlreadyExistsException
import com.bestbidder.infrastructure.config.JwtService
import com.bestbidder.infrastructure.security.RefreshTokenService
import com.bestbidder.usecase.port.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.UUID

class AuthUseCaseTest {
    private val userRepository: UserRepository = mock(UserRepository::class.java)
    private val passwordEncoder: PasswordEncoder = mock(PasswordEncoder::class.java)
    private val jwtService: JwtService = mock(JwtService::class.java)
    private val refreshTokenService: RefreshTokenService = mock(RefreshTokenService::class.java)
    private lateinit var authUseCase: AuthUseCase

    @BeforeEach
    fun setUp() {
        authUseCase = AuthUseCase(userRepository, passwordEncoder, jwtService, refreshTokenService)
    }

    @Nested
    @DisplayName("Register User")
    inner class RegisterTests {
        @Test
        @DisplayName("Debe registrar un usuario correctamente con email y username válidos")
        fun `should register user successfully`() {
            val email = "david@test.com"
            val username = "david_85"
            val password = "secreto85"

            `when`(userRepository.existsByEmail(email)).thenReturn(false)
            `when`(userRepository.existsByUsername(username)).thenReturn(false)
            `when`(passwordEncoder.encode(password)).thenReturn("hashed_password")
            `when`(userRepository.save(any(User::class.java))).thenAnswer { it.arguments[0] as User }

            val result = authUseCase.register(email, username, password)

            assertEquals(email, result.email)
            assertEquals(username, result.username)
            assertEquals("hashed_password", result.passwordHash)
            verify(userRepository, times(1)).save(any(User::class.java))
        }

        @Test
        @DisplayName("Debe lanzar excepción si el email ya está registrado")
        fun `should throw exception when email exists`() {
            `when`(userRepository.existsByEmail("david@test.com")).thenReturn(true)

            val exception =
                assertThrows(IllegalArgumentException::class.java) {
                    authUseCase.register("david@test.com", "david_85", "secreto85")
                }

            assertEquals("El email ya está registrado", exception.message)
            verify(userRepository, never()).save(any(User::class.java))
        }

        @Test
        @DisplayName("Debe lanzar excepción si el username ya existe")
        fun `should throw exception when username exists`() {
            `when`(userRepository.existsByEmail("david@test.com")).thenReturn(false)
            `when`(userRepository.existsByUsername("david_85")).thenReturn(true)

            val exception =
                assertThrows(UsernameAlreadyExistsException::class.java) {
                    authUseCase.register("david@test.com", "david_85", "secreto85")
                }

            assertEquals("El nombre de usuario 'david_85' ya está en uso", exception.message)
            verify(userRepository, never()).save(any(User::class.java))
        }

        @Test
        @DisplayName("Debe lanzar excepción si el email tiene formato inválido")
        fun `should throw exception when email format is invalid`() {
            val exception =
                assertThrows(IllegalArgumentException::class.java) {
                    authUseCase.register("esto-no-es-un-email", "david_85", "secreto85")
                }
            assertEquals("El formato del email no es válido.", exception.message)
        }

        @Test
        @DisplayName("Debe lanzar excepción si el username tiene mayúsculas o no tiene letras")
        fun `should throw exception when username format is invalid`() {
            val msg =
                "El username debe tener entre 3 y 50 caracteres, solo minúsculas, " +
                    "números, guion bajo o punto, y contener al menos una letra."

            val exception1 =
                assertThrows(IllegalArgumentException::class.java) {
                    authUseCase.register("david@test.com", "David85", "secreto85")
                }
            assertEquals(msg, exception1.message)

            val exception2 =
                assertThrows(IllegalArgumentException::class.java) {
                    authUseCase.register("david@test.com", "12345", "secreto85")
                }
            assertEquals(msg, exception2.message)
        }
    }

    @Nested
    @DisplayName("Login User")
    inner class LoginTests {
        private val mockUser =
            User(
                id = UUID.randomUUID(),
                email = "david@test.com",
                username = "david_85",
                passwordHash = "hashed_password",
                description = null,
                avatarNum = null,
            )

        @Test
        @DisplayName("Debe iniciar sesión con username correctamente")
        fun `should login with username`() {
            `when`(userRepository.findByUsername("david_85")).thenReturn(mockUser)
            `when`(passwordEncoder.matches("secreto85", "hashed_password")).thenReturn(true)
            `when`(jwtService.generateAccessToken(mockUser.id, mockUser.username)).thenReturn("fake_access_token")
            `when`(refreshTokenService.generate(mockUser.id)).thenReturn("fake_refresh_token")

            val result = authUseCase.login("david_85", "secreto85")

            assertEquals("fake_access_token", result.accessToken)
            assertEquals("fake_refresh_token", result.refreshToken)
            assertEquals(mockUser, result.user)
        }

        @Test
        @DisplayName("Debe iniciar sesión con email correctamente")
        fun `should login with email`() {
            `when`(userRepository.findByEmail("david@test.com")).thenReturn(mockUser)
            `when`(passwordEncoder.matches("secreto85", "hashed_password")).thenReturn(true)
            `when`(jwtService.generateAccessToken(mockUser.id, mockUser.username)).thenReturn("fake_access_token")
            `when`(refreshTokenService.generate(mockUser.id)).thenReturn("fake_refresh_token")

            val result = authUseCase.login("david@test.com", "secreto85")

            assertEquals("fake_access_token", result.accessToken)
        }

        @Test
        @DisplayName("Debe lanzar excepción si la contraseña es incorrecta")
        fun `should throw exception when password is wrong`() {
            `when`(userRepository.findByUsername("david_85")).thenReturn(mockUser)
            `when`(passwordEncoder.matches("wrong_pass", "hashed_password")).thenReturn(false)

            assertThrows(InvalidCredentialsException::class.java) {
                authUseCase.login("david_85", "wrong_pass")
            }
        }
    }
}
