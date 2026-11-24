package com.bestbidder.usecase.bid

import com.bestbidder.domain.auction.Auction
import com.bestbidder.domain.bid.Bid
import com.bestbidder.domain.card.Card
import com.bestbidder.domain.card.CardState
import com.bestbidder.domain.notification.PushMessages
import com.bestbidder.domain.user.User
import com.bestbidder.usecase.port.AuctionNotificationPort
import com.bestbidder.usecase.port.AuctionRepository
import com.bestbidder.usecase.port.BidRepository
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
import java.util.UUID

class PlaceBidUseCaseTest {
    private val auctionRepository: AuctionRepository = mock(AuctionRepository::class.java)
    private val bidRepository: BidRepository = mock(BidRepository::class.java)
    private val notificationPort: AuctionNotificationPort = mock(AuctionNotificationPort::class.java)
    private val userRepository: UserRepository = mock(UserRepository::class.java)
    private val pushNotificationPort: PushNotificationPort = mock(PushNotificationPort::class.java)
    private val useCase =
        PlaceBidUseCase(
            auctionRepository,
            bidRepository,
            notificationPort,
            userRepository,
            pushNotificationPort,
        )

    private val creatorId = UUID.randomUUID()
    private val sampleCard =
        Card(
            id = UUID.randomUUID(),
            ownerId = creatorId,
            name = "Charizard First Edition",
            image = "charizard.png",
            category = "Pokémon",
            collection = "Base Set",
            state = CardState.NEAR_MINT,
            description = "Perfecto estado",
        )

    private fun buildActiveAuction(auctionId: UUID) =
        Auction(
            id = auctionId,
            creatorId = creatorId,
            creatorUsername = "paula",
            card = sampleCard,
            description = "Perfecto estado",
            minPrize = 100.0,
            startedAt = Instant.now().minusSeconds(3600),
            durationInDays = 1,
        )

    private fun buildActiveUser(
        id: UUID,
        username: String,
    ) = User(
        id = id,
        email = "$username@test.com",
        passwordHash = "hash",
        username = username,
        description = null,
        avatarNum = 1,
    )

    @Test
    @DisplayName("Debe registrar la puja y notificar por SSE y push al creador y al anterior mejor postor")
    fun `should place bid successfully when satisfying minimum bid increment`() {
        val auctionId = UUID.randomUUID()
        val previousBidderId = UUID.randomUUID()
        val newBidderId = UUID.randomUUID()

        val activeAuction = buildActiveAuction(auctionId)
        val newBidder = buildActiveUser(newBidderId, "pujador2")

        val previousBid =
            Bid(
                auctionId = auctionId,
                bidderId = previousBidderId,
                bidderUsername = "pujador1",
                amount = 150.0,
            )

        `when`(auctionRepository.getByIdForUpdate(auctionId)).thenReturn(activeAuction)
        `when`(userRepository.findById(newBidderId)).thenReturn(newBidder)
        `when`(bidRepository.findHighestBidByAuctionId(auctionId)).thenReturn(previousBid)
        `when`(bidRepository.save(any(Bid::class.java))).thenAnswer { it.arguments[0] as Bid }

        val result =
            useCase.execute(
                auctionId = auctionId,
                bidderId = newBidderId,
                bidderUsername = "pujador2",
                amount = 155.0,
            )

        assertNotNull(result)
        assertEquals(155.0, result.amount)
        assertEquals(newBidderId, result.bidderId)

        verify(auctionRepository, times(1)).getByIdForUpdate(auctionId)
        verify(bidRepository, times(1)).save(any(Bid::class.java))
        verify(notificationPort, times(1)).notifyNewBidPlaced(
            auctionId = auctionId,
            newAmount = 155.0,
            bidderUsername = "pujador2",
        )
        verify(pushNotificationPort, times(1)).send(
            listOf(creatorId),
            PushMessages.ownAuctionNewBid(auctionId, sampleCard.name, "pujador2", 155.0),
        )
        verify(pushNotificationPort, times(1)).send(
            listOf(previousBidderId),
            PushMessages.outbid(auctionId, sampleCard.name, 155.0),
        )
    }

    @Test
    @DisplayName("Debe lanzar excepcion si la subasta no existe")
    fun `should throw exception when auction does not exist`() {
        val auctionId = UUID.randomUUID()
        `when`(auctionRepository.getByIdForUpdate(auctionId)).thenReturn(null)

        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                useCase.execute(
                    auctionId = auctionId,
                    bidderId = UUID.randomUUID(),
                    bidderUsername = "pujador1",
                    amount = 150.0,
                )
            }

        assertEquals("La subasta no existe", exception.message)
        verify(bidRepository, never()).save(any(Bid::class.java))
    }

    @Test
    @DisplayName("Debe lanzar excepcion si el creador intenta pujar en su propia subasta")
    fun `should throw exception when creator tries to bid`() {
        val auctionId = UUID.randomUUID()
        val activeAuction = buildActiveAuction(auctionId)

        `when`(auctionRepository.getByIdForUpdate(auctionId)).thenReturn(activeAuction)

        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                useCase.execute(
                    auctionId = auctionId,
                    bidderId = creatorId,
                    bidderUsername = "paula",
                    amount = 150.0,
                )
            }

        assertEquals("El creador de la subasta no puede pujar en ella", exception.message)
        verify(bidRepository, never()).save(any(Bid::class.java))
    }

    @Test
    @DisplayName("Debe lanzar excepcion si el usuario intenta superar su propia puja más alta")
    fun `should throw exception when bidder already holds highest bid`() {
        val auctionId = UUID.randomUUID()
        val bidderId = UUID.randomUUID()
        val activeAuction = buildActiveAuction(auctionId)

        val currentHighestBid =
            Bid(
                auctionId = auctionId,
                bidderId = bidderId,
                bidderUsername = "pujador1",
                amount = 150.0,
            )

        `when`(auctionRepository.getByIdForUpdate(auctionId)).thenReturn(activeAuction)
        `when`(userRepository.findById(bidderId)).thenReturn(buildActiveUser(bidderId, "pujador1"))
        `when`(bidRepository.findHighestBidByAuctionId(auctionId)).thenReturn(currentHighestBid)

        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                useCase.execute(
                    auctionId = auctionId,
                    bidderId = bidderId,
                    bidderUsername = "pujador1",
                    amount = 200.0,
                )
            }

        assertEquals("No puedes pujar si ya tienes la puja más alta actual", exception.message)
        verify(bidRepository, never()).save(any(Bid::class.java))
    }

    @Test
    @DisplayName("Debe lanzar excepcion si el importe es menor al incremento mínimo permitido")
    fun `should throw exception when bid amount is below minimum increment`() {
        val auctionId = UUID.randomUUID()
        val bidderId = UUID.randomUUID()
        val activeAuction = buildActiveAuction(auctionId)

        val currentHighestBid =
            Bid(
                auctionId = auctionId,
                bidderId = UUID.randomUUID(),
                bidderUsername = "pujador1",
                amount = 150.0,
            )

        `when`(auctionRepository.getByIdForUpdate(auctionId)).thenReturn(activeAuction)
        `when`(userRepository.findById(bidderId)).thenReturn(buildActiveUser(bidderId, "pujador2"))
        `when`(bidRepository.findHighestBidByAuctionId(auctionId)).thenReturn(currentHighestBid)

        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                useCase.execute(
                    auctionId = auctionId,
                    bidderId = bidderId,
                    bidderUsername = "pujador2",
                    amount = 151.0,
                )
            }

        assertEquals("El importe mínimo para pujar debe ser de 155.0 €", exception.message)
        verify(bidRepository, never()).save(any(Bid::class.java))
    }
}
