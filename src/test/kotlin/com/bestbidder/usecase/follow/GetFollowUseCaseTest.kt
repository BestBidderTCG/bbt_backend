package com.bestbidder.usecase.follow

import com.bestbidder.domain.auction.AuctionListItem
import com.bestbidder.domain.auction.AuctionStatus
import com.bestbidder.domain.card.CardState
import com.bestbidder.domain.follow.FollowListItem
import com.bestbidder.domain.user.User
import com.bestbidder.domain.user.exceptions.UserNotFoundException
import com.bestbidder.usecase.port.FollowRepository
import com.bestbidder.usecase.port.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.time.Instant
import java.util.UUID

class GetFollowUseCaseTest {
    private val followRepository: FollowRepository = mock(FollowRepository::class.java)
    private val userRepository: UserRepository = mock(UserRepository::class.java)
    private val useCase = GetFollowUseCase(followRepository, userRepository)

    private val userId = UUID.randomUUID()
    private val viewerId = UUID.randomUUID()

    private val user =
        User(
            id = userId,
            email = "user@test.com",
            passwordHash = "hash",
            username = "user",
            description = null,
            avatarNum = 1,
        )

    private val followed =
        FollowListItem(
            id = UUID.randomUUID(),
            username = "amigo",
            avatarNum = 2,
            followedByMe = true,
        )

    private fun auction(status: AuctionStatus) =
        AuctionListItem(
            id = UUID.randomUUID(),
            cardName = "Charizard",
            cardImage = "charizard.png",
            currentBid = 100.0,
            remainingDays = 3,
            cardState = CardState.NEW,
            cardCategory = "Pokémon",
            cardCollection = null,
            cardNumber = null,
            status = status,
        )

    @Nested
    @DisplayName("getFollowing")
    inner class GetFollowingTests {
        @Test
        @DisplayName("Debe devolver los seguidos del usuario")
        fun `should return following`() {
            `when`(userRepository.findById(userId)).thenReturn(user)
            `when`(followRepository.getFollowing(userId, viewerId, 0, 20)).thenReturn(listOf(followed))

            val result = useCase.getFollowing(userId, viewerId, 0, 20)

            assertEquals(listOf(followed), result)
            verify(followRepository, times(1)).getFollowing(userId, viewerId, 0, 20)
        }

        @Test
        @DisplayName("Debe limitar el tamaño de página a 50 y corregir páginas negativas")
        fun `should clamp page and size`() {
            `when`(userRepository.findById(userId)).thenReturn(user)
            `when`(followRepository.getFollowing(userId, viewerId, 0, GetFollowUseCase.MAX_PAGE_SIZE))
                .thenReturn(listOf(followed))

            val result = useCase.getFollowing(userId, viewerId, -3, 500)

            assertEquals(1, result.size)
            verify(followRepository, times(1)).getFollowing(userId, viewerId, 0, GetFollowUseCase.MAX_PAGE_SIZE)
        }

        @Test
        @DisplayName("Debe usar tamaño mínimo 1 cuando el tamaño es 0 o negativo")
        fun `should use minimum size of one`() {
            `when`(userRepository.findById(userId)).thenReturn(user)

            useCase.getFollowing(userId, viewerId, 0, 0)

            verify(followRepository, times(1)).getFollowing(userId, viewerId, 0, 1)
        }

        @Test
        @DisplayName("Debe lanzar UserNotFoundException cuando el usuario no existe")
        fun `should throw when user does not exist`() {
            `when`(userRepository.findById(userId)).thenReturn(null)

            assertThrows(UserNotFoundException::class.java) {
                useCase.getFollowing(userId, viewerId, 0, 20)
            }

            verify(followRepository, never()).getFollowing(userId, viewerId, 0, 20)
        }

        @Test
        @DisplayName("Debe lanzar UserNotFoundException cuando la cuenta está eliminada")
        fun `should throw when user is deleted`() {
            `when`(userRepository.findById(userId)).thenReturn(user.copy(deletedAt = Instant.now()))

            assertThrows(UserNotFoundException::class.java) {
                useCase.getFollowing(userId, viewerId, 0, 20)
            }
        }
    }

    @Nested
    @DisplayName("getFollowers")
    inner class GetFollowersTests {
        @Test
        @DisplayName("Debe devolver los seguidores del usuario")
        fun `should return followers`() {
            `when`(userRepository.findById(userId)).thenReturn(user)
            `when`(followRepository.getFollowers(userId, viewerId, 1, 10)).thenReturn(listOf(followed))

            val result = useCase.getFollowers(userId, viewerId, 1, 10)

            assertEquals(listOf(followed), result)
            verify(followRepository, times(1)).getFollowers(userId, viewerId, 1, 10)
        }

        @Test
        @DisplayName("Debe lanzar UserNotFoundException cuando el usuario no existe")
        fun `should throw when user does not exist`() {
            `when`(userRepository.findById(userId)).thenReturn(null)

            assertThrows(UserNotFoundException::class.java) {
                useCase.getFollowers(userId, viewerId, 0, 20)
            }
        }
    }

    @Nested
    @DisplayName("getStats")
    inner class GetStatsTests {
        @Test
        @DisplayName("Debe devolver contadores y si el consultante sigue al usuario")
        fun `should return stats`() {
            `when`(userRepository.findById(userId)).thenReturn(user)
            `when`(followRepository.countFollowers(userId)).thenReturn(12L)
            `when`(followRepository.countFollowing(userId)).thenReturn(3L)
            `when`(followRepository.isFollowing(viewerId, userId)).thenReturn(true)

            val stats = useCase.getStats(userId, viewerId)

            assertEquals(12L, stats.followersCount)
            assertEquals(3L, stats.followingCount)
            assertTrue(stats.following)
        }

        @Test
        @DisplayName("Debe indicar following=false cuando el consultante no lo sigue")
        fun `should return following false`() {
            `when`(userRepository.findById(userId)).thenReturn(user)
            `when`(followRepository.isFollowing(viewerId, userId)).thenReturn(false)

            assertFalse(useCase.getStats(userId, viewerId).following)
        }

        @Test
        @DisplayName("Debe lanzar UserNotFoundException cuando el usuario no existe")
        fun `should throw when user does not exist`() {
            `when`(userRepository.findById(userId)).thenReturn(null)

            assertThrows(UserNotFoundException::class.java) {
                useCase.getStats(userId, viewerId)
            }
        }
    }

    @Nested
    @DisplayName("getFollowingAuctions")
    inner class GetFollowingAuctionsTests {
        private val upcoming = auction(AuctionStatus.UPCOMING)
        private val inProgress = auction(AuctionStatus.IN_PROGRESS)

        @Test
        @DisplayName("Sin filtro debe devolver activas y próximas")
        fun `should return all without filter`() {
            `when`(followRepository.getFollowingAuctions(userId)).thenReturn(listOf(inProgress, upcoming))

            val result = useCase.getFollowingAuctions(userId)

            assertEquals(listOf(inProgress, upcoming), result)
        }

        @Test
        @DisplayName("Con status UPCOMING debe devolver solo las próximas")
        fun `should filter upcoming`() {
            `when`(followRepository.getFollowingAuctions(userId)).thenReturn(listOf(inProgress, upcoming))

            val result = useCase.getFollowingAuctions(userId, AuctionStatus.UPCOMING)

            assertEquals(listOf(upcoming), result)
        }

        @Test
        @DisplayName("Con status IN_PROGRESS debe devolver solo las activas")
        fun `should filter in progress`() {
            `when`(followRepository.getFollowingAuctions(userId)).thenReturn(listOf(inProgress, upcoming))

            val result = useCase.getFollowingAuctions(userId, AuctionStatus.IN_PROGRESS)

            assertEquals(listOf(inProgress), result)
        }

        @Test
        @DisplayName("Debe lanzar IllegalArgumentException con un estado distinto de UPCOMING o IN_PROGRESS")
        fun `should reject finished status`() {
            val exception =
                assertThrows(IllegalArgumentException::class.java) {
                    useCase.getFollowingAuctions(userId, AuctionStatus.FINISHED)
                }

            assertEquals("El estado solo puede ser UPCOMING o IN_PROGRESS", exception.message)
            verify(followRepository, never()).getFollowingAuctions(userId)
        }

        @Test
        @DisplayName("Debe devolver lista vacía cuando los seguidos no tienen subastas")
        fun `should return empty list`() {
            `when`(followRepository.getFollowingAuctions(userId)).thenReturn(emptyList())

            assertEquals(emptyList<AuctionListItem>(), useCase.getFollowingAuctions(userId))
        }
    }
}
