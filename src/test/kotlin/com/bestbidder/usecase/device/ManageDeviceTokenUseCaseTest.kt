package com.bestbidder.usecase.device

import com.bestbidder.domain.device.DevicePlatform
import com.bestbidder.usecase.port.DeviceTokenRepository
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.util.UUID

class ManageDeviceTokenUseCaseTest {
    private val deviceTokenRepository: DeviceTokenRepository = mock(DeviceTokenRepository::class.java)
    private val useCase = ManageDeviceTokenUseCase(deviceTokenRepository)

    private val userId = UUID.randomUUID()

    @Test
    @DisplayName("Debe registrar el token recortando espacios")
    fun `should register trimmed token`() {
        useCase.register(userId, "  abc123  ", DevicePlatform.ANDROID)

        verify(deviceTokenRepository, times(1)).register(userId, "abc123", DevicePlatform.ANDROID)
    }

    @Test
    @DisplayName("Debe rechazar un token vacío")
    fun `should reject blank token`() {
        assertThrows(IllegalArgumentException::class.java) {
            useCase.register(userId, "   ", DevicePlatform.IOS)
        }
    }

    @Test
    @DisplayName("Debe rechazar un token demasiado largo")
    fun `should reject too long token`() {
        assertThrows(IllegalArgumentException::class.java) {
            useCase.register(userId, "x".repeat(513), DevicePlatform.IOS)
        }
    }

    @Test
    @DisplayName("Debe eliminar el token y devolver si existía")
    fun `should unregister token`() {
        `when`(deviceTokenRepository.remove(userId, "abc123")).thenReturn(true)
        `when`(deviceTokenRepository.remove(userId, "zzz")).thenReturn(false)

        assertTrue(useCase.unregister(userId, "abc123"))
        assertFalse(useCase.unregister(userId, "zzz"))
    }
}
