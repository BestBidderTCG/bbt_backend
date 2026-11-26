package com.bestbidder.usecase.user

import com.bestbidder.domain.user.User
import com.bestbidder.domain.user.exceptions.UserNotFoundException
import com.bestbidder.usecase.port.AuctionRepository
import com.bestbidder.usecase.port.BidRepository
import com.bestbidder.usecase.port.CardRepository
import com.bestbidder.usecase.port.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.time.Instant
import java.util.UUID

class GetUserUseCaseTest {
    private val userRepository: UserRepository = mock(UserRepository::class.java)
    private val auctionRepository: AuctionRepository = mock(AuctionRepository::class.java)
    private val bidRepository: BidRepository = mock(BidRepository::class.java)
    private val cardRepository: CardRepository = mock(CardRepository::class.java)
    private val useCase = GetUserUseCase(userRepository, auctionRepository, bidRepository, cardRepository)
    private val userId = UUID.randomUUID()

    private fun buildUser(deletedAt: Instant? = null) =
        User(
            id = userId,
            email = "david@test.com",
            passwordHash = "hashed",
            username = "david_85",
            description = null,
            avatarNum = null,
            deletedAt = deletedAt,
        )

    @Test
    @DisplayName("Debe retornar el usuario si existe")
    fun `should return user when exists`() {
        val user = buildUser()
        `when`(userRepository.findById(userId)).thenReturn(user)

        assertEquals(user, useCase.getById(userId))
    }

    @Test
    @DisplayName("Debe lanzar excepción si el usuario no existe")
    fun `should throw exception when user not found`() {
        `when`(userRepository.findById(userId)).thenReturn(null)

        assertThrows(UserNotFoundException::class.java) { useCase.getById(userId) }
    }

    @Test
    @DisplayName("Debe lanzar excepción si el usuario está borrado")
    fun `should throw exception when user is deleted`() {
        `when`(userRepository.findById(userId)).thenReturn(buildUser(deletedAt = Instant.now()))

        assertThrows(UserNotFoundException::class.java) { useCase.getById(userId) }
    }

    @Test
    @DisplayName("getProfile devuelve el usuario con sus contadores de subastas, pujas y cartas")
    fun `should return profile with stats`() {
        val user = buildUser()
        `when`(userRepository.findById(userId)).thenReturn(user)
        `when`(auctionRepository.countByCreatorId(userId)).thenReturn(2L)
        `when`(bidRepository.countByBidderId(userId)).thenReturn(5L)
        `when`(cardRepository.countByOwnerId(userId)).thenReturn(10L)

        val result = useCase.getProfile(userId)

        assertEquals(user, result.user)
        assertEquals(2L, result.stats.auctionCount)
        assertEquals(5L, result.stats.bidCount)
        assertEquals(10L, result.stats.cardCount)
    }

    @Test
    @DisplayName("getProfile lanza la excepción y no consulta los contadores si el usuario no existe")
    fun `should not count when profile user not found`() {
        `when`(userRepository.findById(userId)).thenReturn(null)

        assertThrows(UserNotFoundException::class.java) { useCase.getProfile(userId) }

        verify(auctionRepository, never()).countByCreatorId(any(UUID::class.java))
        verify(bidRepository, never()).countByBidderId(any(UUID::class.java))
        verify(cardRepository, never()).countByOwnerId(any(UUID::class.java))
    }
}
