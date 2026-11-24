package com.bestbidder.usecase.auction

import com.bestbidder.domain.auction.Auction
import com.bestbidder.domain.card.Card
import com.bestbidder.domain.card.CardState
import com.bestbidder.domain.notification.PushMessages
import com.bestbidder.domain.user.User
import com.bestbidder.usecase.port.AuctionRepository
import com.bestbidder.usecase.port.FollowRepository
import com.bestbidder.usecase.port.PushNotificationPort
import com.bestbidder.usecase.port.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

class CreateAuctionUseCaseTest {
    private val auctionRepository: AuctionRepository = mock(AuctionRepository::class.java)
    private val userRepository: UserRepository = mock(UserRepository::class.java)
    private val followRepository: FollowRepository = mock(FollowRepository::class.java)
    private val pushNotificationPort: PushNotificationPort = mock(PushNotificationPort::class.java)
    private val useCase =
        CreateAuctionUseCase(auctionRepository, userRepository, followRepository, pushNotificationPort)

    private fun getValidFutureStart(): Instant {
        val nowUtc = ZonedDateTime.now(ZoneOffset.UTC)
        val currentMinute = nowUtc.minute
        val minutesToAdd = 15 - (currentMinute % 15)

        return nowUtc.plusMinutes(minutesToAdd.toLong())
            .truncatedTo(ChronoUnit.MINUTES)
            .toInstant()
    }

    @Test
    @DisplayName("Debe crear y guardar la subasta y notificar a los seguidores si cumple las validaciones")
    fun `should create auction successfully with description`() {
        val creatorId = UUID.randomUUID()
        val followerId = UUID.randomUUID()
        val creatorUsername = "paula"
        val creator =
            User(
                id = creatorId,
                email = "paula@test.com",
                passwordHash = "hash",
                username = creatorUsername,
                description = null,
                avatarNum = 1,
            )
        val card =
            Card(
                id = UUID.randomUUID(),
                ownerId = creatorId,
                name = "Pikachu Illustrator",
                image = "pikachu.png",
                category = "Pokémon",
                state = CardState.NEW,
            )
        val description = "Carta en perfecto estado PSA 10"
        val validStart = getValidFutureStart()

        `when`(userRepository.findById(creatorId)).thenReturn(creator)
        `when`(auctionRepository.existsByCreatorAndCard(creatorUsername, card.id)).thenReturn(false)
        `when`(auctionRepository.save(any())).thenAnswer { invocation -> invocation.getArgument(0) as Auction }
        `when`(followRepository.findFollowerIdsByUserId(creatorId)).thenReturn(listOf(followerId))

        val result =
            useCase.create(
                creatorId = creatorId,
                creatorUsername = creatorUsername,
                card = card,
                description = description,
                minPrize = 500.0,
                startedAt = validStart,
                durationInDays = 3,
            )

        assertNotNull(result)
        assertEquals(creatorId, result.creatorId)
        assertEquals(creatorUsername, result.creatorUsername)
        assertEquals(card, result.card)
        assertEquals(description, result.description)

        verify(auctionRepository, times(1)).existsByCreatorAndCard(creatorUsername, card.id)
        verify(auctionRepository, times(1)).save(any())
        verify(pushNotificationPort, times(1)).send(
            listOf(followerId),
            PushMessages.followeeCreatedAuction(result.id, creatorUsername, card.name),
        )
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException si ya existe una subasta de esa carta y usuario")
    fun `should throw exception when duplicate auction exists`() {
        val creatorId = UUID.randomUUID()
        val creatorUsername = "paula"
        val creator =
            User(
                id = creatorId,
                email = "paula@test.com",
                passwordHash = "hash",
                username = creatorUsername,
                description = null,
                avatarNum = 1,
            )
        val card =
            Card(
                id = UUID.randomUUID(),
                ownerId = creatorId,
                name = "Charizard 1st Edition",
                image = "charizard.png",
                category = "Pokémon",
                state = CardState.GOOD,
            )
        val validStart = getValidFutureStart()

        `when`(userRepository.findById(creatorId)).thenReturn(creator)
        `when`(auctionRepository.existsByCreatorAndCard(creatorUsername, card.id)).thenReturn(true)

        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                useCase.create(
                    creatorId = creatorId,
                    creatorUsername = creatorUsername,
                    card = card,
                    description = "Primera edición",
                    minPrize = 150.0,
                    startedAt = validStart,
                    durationInDays = 2,
                )
            }

        assertEquals(
            "Ya existe una subasta activa o próxima para la carta con ID '${card.id}' y el usuario '$creatorUsername'",
            exception.message,
        )

        verify(auctionRepository, times(1)).existsByCreatorAndCard(creatorUsername, card.id)
        verify(auctionRepository, never()).save(any())
    }
}
