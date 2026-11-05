package com.bestbidder.infrastructure.security

import com.bestbidder.domain.user.exceptions.InvalidRefreshTokenException
import com.bestbidder.usecase.port.RefreshTokenRepository
import com.bestbidder.usecase.port.StoredRefreshToken
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.time.LocalDateTime
import java.util.UUID

class RefreshTokenServiceTest {
    private val repository: RefreshTokenRepository = mock(RefreshTokenRepository::class.java)
    private val expirationDays = 30L
    private val refreshTokenService = RefreshTokenService(repository, expirationDays)
    private val userId = UUID.randomUUID()

    @Test
    @DisplayName("Debe generar un refresh token y guardarlo hasheado en la BD")
    fun `should generate and save a refresh token`() {
        val token = refreshTokenService.generate(userId)
        assertEquals(86, token.length)
        verify(repository, times(1)).save(eq(userId), anyString(), any())
    }

    @Test
    @DisplayName("Debe validar y consumir un token correcto eliminándolo de la BD")
    fun `should validate and consume a valid token`() {
        val rawToken = "fake-refresh-token"
        val hashedToken = hashTokenForTest(rawToken)
        val futureDate = LocalDateTime.now().plusDays(10)

        `when`(repository.findByHash(hashedToken)).thenReturn(StoredRefreshToken(userId, futureDate))

        val resultUserId = refreshTokenService.validateAndConsume(rawToken)

        assertEquals(userId, resultUserId)
        verify(repository, times(1)).deleteByHash(hashedToken)
    }

    @Test
    @DisplayName("Debe lanzar excepción si el token no existe en la BD")
    fun `should throw exception when token does not exist`() {
        `when`(repository.findByHash(anyString())).thenReturn(null)

        assertThrows(InvalidRefreshTokenException::class.java) {
            refreshTokenService.validateAndConsume("token-que-no-existe")
        }
        verify(repository, never()).deleteByHash(anyString())
    }

    @Test
    @DisplayName("Debe lanzar excepción si el token ha expirado")
    fun `should throw exception when token is expired`() {
        val rawToken = "expired-token"
        val hashedToken = hashTokenForTest(rawToken)
        val pastDate = LocalDateTime.now().minusDays(1)

        `when`(repository.findByHash(hashedToken)).thenReturn(StoredRefreshToken(userId, pastDate))

        assertThrows(InvalidRefreshTokenException::class.java) {
            refreshTokenService.validateAndConsume(rawToken)
        }
        verify(repository, times(1)).deleteByHash(hashedToken)
    }

    private fun hashTokenForTest(input: String): String {
        return java.security.MessageDigest.getInstance("SHA-256")
            .digest(input.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }
}
