package com.bestbidder.usecase.auction

import com.bestbidder.domain.auction.Auction
import com.bestbidder.domain.card.Card
import com.bestbidder.domain.card.CardState
import com.bestbidder.domain.notification.Audience
import com.bestbidder.domain.notification.SseEvents
import com.bestbidder.usecase.port.AuctionRepository
import com.bestbidder.usecase.port.RealtimeEventPort
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

class EditAuctionUseCaseTest {
    private lateinit var auctionRepository: AuctionRepository
    private lateinit var realtimeEventPort: RealtimeEventPort
    private lateinit var useCase: EditAuctionUseCase

    private val creatorId = UUID.randomUUID()
    private val auctionId = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        auctionRepository = mock(AuctionRepository::class.java)
        realtimeEventPort = mock(RealtimeEventPort::class.java)
        useCase = EditAuctionUseCase(auctionRepository, realtimeEventPort)
    }

    private fun buildAuction(
        startedAt: Instant = Instant.now().plus(2, ChronoUnit.DAYS),
        minPrize: Double = 50.0,
        durationInDays: Int = 3,
        description: String? = "Descripción inicial",
    ): Auction {
        val card =
            Card(
                id = UUID.randomUUID(),
                ownerId = creatorId,
                name = "Charizard",
                image = "/uploads/charizard.png",
                category = "Pokemon",
                collection = "Base Set",
                num = 4,
                state = CardState.NEAR_MINT,
                description = null,
                createdAt = Instant.now().minus(1, ChronoUnit.DAYS),
            )
        return Auction(
            id = auctionId,
            creatorId = creatorId,
            creatorUsername = "paula",
            card = card,
            description = description,
            minPrize = minPrize,
            startedAt = startedAt,
            durationInDays = durationInDays,
        )
    }

    @Test
    fun `edit updates only provided fields and saves auction`() {
        val original = buildAuction()
        val newStart = Instant.now().plus(5, ChronoUnit.DAYS)
        val captor = ArgumentCaptor.forClass(Auction::class.java)

        `when`(auctionRepository.getByIdForUpdate(auctionId)).thenReturn(original)
        `when`(auctionRepository.save(any(Auction::class.java))).thenAnswer { invocation ->
            invocation.getArgument<Auction>(0)
        }

        val result =
            useCase.edit(
                EditAuctionCommand(
                    auctionId = auctionId,
                    requesterUserId = creatorId,
                    description = "Nueva descripción",
                    minPrize = 75.0,
                    startedAt = newStart,
                    durationInDays = null,
                ),
            )

        verify(auctionRepository, times(1)).save(captor.capture())
        assertEquals("Nueva descripción", result.description)
        assertEquals(75.0, result.minPrize)
        assertEquals(newStart, result.startedAt)
        assertEquals(3, result.durationInDays)
        assertEquals("Nueva descripción", captor.value.description)
        verify(realtimeEventPort, times(1)).publish(SseEvents.auctionUpdated(auctionId), Audience.Everyone)
    }

    @Test
    fun `edit throws IllegalArgumentException when auction does not exist`() {
        `when`(auctionRepository.getByIdForUpdate(auctionId)).thenReturn(null)

        assertThrows<IllegalArgumentException> {
            useCase.edit(EditAuctionCommand(auctionId = auctionId, requesterUserId = creatorId))
        }
        verify(auctionRepository, never()).save(any(Auction::class.java))
    }

    @Test
    fun `edit throws IllegalArgumentException when requester is not the creator`() {
        val otherUserId = UUID.randomUUID()
        `when`(auctionRepository.getByIdForUpdate(auctionId)).thenReturn(buildAuction())

        assertThrows<IllegalArgumentException> {
            useCase.edit(EditAuctionCommand(auctionId = auctionId, requesterUserId = otherUserId))
        }
        verify(auctionRepository, never()).save(any(Auction::class.java))
        verifyNoInteractions(realtimeEventPort)
    }

    @Test
    fun `edit throws IllegalStateException when auction has already started`() {
        val startedAuction = buildAuction(startedAt = Instant.now().minus(1, ChronoUnit.HOURS))
        `when`(auctionRepository.getByIdForUpdate(auctionId)).thenReturn(startedAuction)

        assertThrows<IllegalStateException> {
            useCase.edit(
                EditAuctionCommand(
                    auctionId = auctionId,
                    requesterUserId = creatorId,
                    minPrize = 100.0,
                ),
            )
        }
        verify(auctionRepository, never()).save(any(Auction::class.java))
    }

    @Test
    fun `edit throws IllegalArgumentException when minPrize is zero or negative`() {
        `when`(auctionRepository.getByIdForUpdate(auctionId)).thenReturn(buildAuction())

        assertThrows<IllegalArgumentException> {
            useCase.edit(
                EditAuctionCommand(
                    auctionId = auctionId,
                    requesterUserId = creatorId,
                    minPrize = 0.0,
                ),
            )
        }
        verify(auctionRepository, never()).save(any(Auction::class.java))
    }

    @Test
    fun `edit throws IllegalArgumentException when durationInDays is less than 1`() {
        `when`(auctionRepository.getByIdForUpdate(auctionId)).thenReturn(buildAuction())

        assertThrows<IllegalArgumentException> {
            useCase.edit(
                EditAuctionCommand(
                    auctionId = auctionId,
                    requesterUserId = creatorId,
                    durationInDays = 0,
                ),
            )
        }
        verify(auctionRepository, never()).save(any(Auction::class.java))
    }

    @Test
    fun `edit throws IllegalArgumentException when new startedAt is in the past`() {
        `when`(auctionRepository.getByIdForUpdate(auctionId)).thenReturn(buildAuction())

        assertThrows<IllegalArgumentException> {
            useCase.edit(
                EditAuctionCommand(
                    auctionId = auctionId,
                    requesterUserId = creatorId,
                    startedAt = Instant.now().minus(10, ChronoUnit.MINUTES),
                ),
            )
        }
        verify(auctionRepository, never()).save(any(Auction::class.java))
    }
}
