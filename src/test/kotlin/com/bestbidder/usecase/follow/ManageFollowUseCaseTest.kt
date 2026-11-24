package com.bestbidder.usecase.follow

import com.bestbidder.domain.follow.Follow
import com.bestbidder.domain.notification.PushMessages
import com.bestbidder.domain.user.User
import com.bestbidder.domain.user.exceptions.UserNotFoundException
import com.bestbidder.usecase.port.FollowRepository
import com.bestbidder.usecase.port.PushNotificationPort
import com.bestbidder.usecase.port.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.time.Instant
import java.util.UUID

class ManageFollowUseCaseTest {
    private val followRepository: FollowRepository = mock(FollowRepository::class.java)
    private val userRepository: UserRepository = mock(UserRepository::class.java)
    private val pushNotificationPort: PushNotificationPort = mock(PushNotificationPort::class.java)
    private val useCase = ManageFollowUseCase(followRepository, userRepository, pushNotificationPort)

    private val followerId = UUID.randomUUID()
    private val followeeId = UUID.randomUUID()

    private val followee =
        User(
            id = followeeId,
            email = "followee@test.com",
            passwordHash = "hash",
            username = "followee",
            description = null,
            avatarNum = 1,
        )

    private val follower =
        User(
            id = followerId,
            email = "follower@test.com",
            passwordHash = "hash",
            username = "follower",
            description = null,
            avatarNum = 2,
        )

    // Mockito devuelve null en any()/capture(); estos helpers evitan el NPE de Kotlin con parámetros no nulos.
    @Suppress("UNCHECKED_CAST")
    private fun <T> anyObject(): T {
        ArgumentMatchers.any<T>()
        return null as T
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> ArgumentCaptor<T>.captureNonNull(): T {
        capture()
        return null as T
    }

    @Nested
    @DisplayName("follow")
    inner class FollowTests {
        @Test
        @DisplayName("Debe crear el follow cuando el usuario existe y devolver true")
        fun `should follow when followee exists`() {
            `when`(userRepository.findById(followeeId)).thenReturn(followee)
            `when`(userRepository.findById(followerId)).thenReturn(follower)
            `when`(followRepository.follow(anyObject())).thenReturn(true)

            val created = useCase.follow(followerId, followeeId)

            assertTrue(created)
            val captor = ArgumentCaptor.forClass(Follow::class.java)
            verify(followRepository, times(1)).follow(captor.captureNonNull())
            assertEquals(followerId, captor.value.followerId)
            assertEquals(followeeId, captor.value.followeeId)
        }

        @Test
        @DisplayName("Debe enviar push al seguido cuando el follow es nuevo")
        fun `should push the followee when follow is new`() {
            `when`(userRepository.findById(followeeId)).thenReturn(followee)
            `when`(userRepository.findById(followerId)).thenReturn(follower)
            `when`(followRepository.follow(anyObject())).thenReturn(true)

            useCase.follow(followerId, followeeId)

            verify(pushNotificationPort, times(1))
                .send(listOf(followeeId), PushMessages.newFollower(followerId, "follower"))
        }

        @Test
        @DisplayName("Debe devolver false cuando ya seguía al usuario")
        fun `should return false when already following`() {
            `when`(userRepository.findById(followeeId)).thenReturn(followee)
            `when`(followRepository.follow(anyObject())).thenReturn(false)

            val created = useCase.follow(followerId, followeeId)

            assertFalse(created)
            verify(pushNotificationPort, never()).send(anyObject(), anyObject())
        }

        @Test
        @DisplayName("Debe lanzar IllegalArgumentException cuando intenta seguirse a sí mismo")
        fun `should throw when following yourself`() {
            val exception =
                assertThrows(IllegalArgumentException::class.java) {
                    useCase.follow(followerId, followerId)
                }

            assertEquals("No puedes seguirte a ti mismo", exception.message)
            verify(followRepository, never()).follow(anyObject())
        }

        @Test
        @DisplayName("Debe lanzar UserNotFoundException cuando el usuario no existe")
        fun `should throw UserNotFoundException when followee does not exist`() {
            `when`(userRepository.findById(followeeId)).thenReturn(null)

            assertThrows(UserNotFoundException::class.java) {
                useCase.follow(followerId, followeeId)
            }

            verify(followRepository, never()).follow(anyObject())
        }

        @Test
        @DisplayName("Debe lanzar UserNotFoundException cuando el usuario tiene la cuenta eliminada")
        fun `should throw UserNotFoundException when followee is deleted`() {
            `when`(userRepository.findById(followeeId)).thenReturn(followee.copy(deletedAt = Instant.now()))

            assertThrows(UserNotFoundException::class.java) {
                useCase.follow(followerId, followeeId)
            }

            verify(followRepository, never()).follow(anyObject())
        }
    }

    @Nested
    @DisplayName("unfollow")
    inner class UnfollowTests {
        @Test
        @DisplayName("Debe delegar en el repositorio y devolver true si existía la relación")
        fun `should unfollow`() {
            `when`(followRepository.unfollow(followerId, followeeId)).thenReturn(true)

            val removed = useCase.unfollow(followerId, followeeId)

            assertTrue(removed)
            verify(followRepository, times(1)).unfollow(followerId, followeeId)
        }

        @Test
        @DisplayName("Debe devolver false si no seguía al usuario")
        fun `should return false when not following`() {
            `when`(followRepository.unfollow(followerId, followeeId)).thenReturn(false)

            assertFalse(useCase.unfollow(followerId, followeeId))
        }
    }
}
