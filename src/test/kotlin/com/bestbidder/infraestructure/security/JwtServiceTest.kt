package com.bestbidder.infrastructure.security

import com.bestbidder.infrastructure.config.JwtService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.UUID

class JwtServiceTest {
    // Clave de mínimo 32 caracteres y 15 min para caducar
    private val secret = "clave-secreta-para-tests-de-mas-de-32-caracteres"
    private val expirationMs = 900000L
    private val jwtService = JwtService(secret, expirationMs)

    @Test
    @DisplayName("Debe generar un Access Token válido y parsearlo correctamente")
    fun `should generate and parse a valid access token`() {
        val userId = UUID.randomUUID()
        val username = "david_85"

        val token = jwtService.generateAccessToken(userId, username)
        assertNotNull(token)

        val parsed = jwtService.parseAccessToken(token)
        assertNotNull(parsed)
        assertEquals(userId, parsed?.userId)
        assertEquals(username, parsed?.username)
    }

    @Test
    @DisplayName("Debe retornar null al parsear un token inválido")
    fun `should return null for an invalid token`() {
        val invalidToken = "eyJhbGciOiJIUzI1NiJ9.falso.firma"
        val parsed = jwtService.parseAccessToken(invalidToken)
        assertNull(parsed)
    }
}
