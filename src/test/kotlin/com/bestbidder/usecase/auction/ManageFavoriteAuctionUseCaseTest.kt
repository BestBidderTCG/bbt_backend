package com.bestbidder.usecase.auction

import com.bestbidder.domain.auction.Auction
import com.bestbidder.domain.auction.AuctionListItem
import com.bestbidder.domain.auction.AuctionStatus
import com.bestbidder.domain.auction.FavoriteAuctionItem
import com.bestbidder.domain.auction.NotificationMode
import com.bestbidder.domain.card.Card
import com.bestbidder.domain.card.CardState
import com.bestbidder.usecase.port.AuctionRepository
import com.bestbidder.usecase.port.FavoriteAuctionRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
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

class ManageFavoriteAuctionUseCaseTest {
    private val favoriteAuctionRepository: FavoriteAuctionRepository = mock(FavoriteAuctionRepository::class.java)
    private val auctionRepository: AuctionRepository = mock(AuctionRepository::class.java)
    private val useCase = ManageFavoriteAuctionUseCase(favoriteAuctionRepository, auctionRepository)

    private val ownerId = UUID.randomUUID()
    private val otherUserId = UUID.randomUUID()
    private val auctionId = UUID.randomUUID()
    private val now = Instant.now()

    private val sampleCard =
        Card(
            id = UUID.randomUUID(),
            ownerId = ownerId,
            name = "Pikachu Illustrator",
            image = "pikachu.png",
            category = "Pokémon",
            state = CardState.NEW,
        )

    private val sampleAuction =
        Auction(
            id = auctionId,
            creatorId = ownerId,
            creatorUsername = "user1",
            card = sampleCard,
            description = "Subasta de prueba",
            minPrize = 100.0,
            startedAt = now,
            durationInDays = 3,
        )

    @Nested
    @DisplayName("addFavorite")
    inner class AddFavoriteTests {
        @Test
        @DisplayName("Debe guardar la subasta en favoritos cuando existe y pertenece a otro usuario")
        fun `should add favorite when auction exists and belongs to another user`() {
            `when`(auctionRepository.getById(auctionId)).thenReturn(sampleAuction)

            useCase.addFavorite(otherUserId, auctionId, NotificationMode.ALL)

            verify(favoriteAuctionRepository, times(1))
                .addFavorite(otherUserId, auctionId, NotificationMode.ALL)
        }

        @Test
        @DisplayName("Debe lanzar IllegalArgumentException cuando la subasta no existe")
        fun `should throw IllegalArgumentException when auction does not exist`() {
            `when`(auctionRepository.getById(auctionId)).thenReturn(null)

            val exception =
                assertThrows(IllegalArgumentException::class.java) {
                    useCase.addFavorite(otherUserId, auctionId, NotificationMode.BASIC)
                }

            assertEquals("No existe la subasta con ID $auctionId", exception.message)
            verify(favoriteAuctionRepository, never())
                .addFavorite(otherUserId, auctionId, NotificationMode.BASIC)
        }

        @Test
        @DisplayName("Debe lanzar IllegalArgumentException cuando el usuario intenta guardar su propia subasta")
        fun `should throw IllegalArgumentException when user tries to favorite own auction`() {
            `when`(auctionRepository.getById(auctionId)).thenReturn(sampleAuction)

            val exception =
                assertThrows(IllegalArgumentException::class.java) {
                    useCase.addFavorite(ownerId, auctionId, NotificationMode.BASIC)
                }

            assertEquals("No puedes añadir tu propia subasta a favoritos", exception.message)
            verify(favoriteAuctionRepository, never())
                .addFavorite(ownerId, auctionId, NotificationMode.BASIC)
        }
    }

    @Nested
    @DisplayName("removeFavorite")
    inner class RemoveFavoriteTests {
        @Test
        @DisplayName("Debe delegar en el repositorio la eliminación del favorito")
        fun `should remove favorite auction`() {
            useCase.removeFavorite(otherUserId, auctionId)

            verify(favoriteAuctionRepository, times(1)).removeFavorite(otherUserId, auctionId)
        }
    }

    @Nested
    @DisplayName("changeNotificationMode")
    inner class ChangeNotificationModeTests {
        @Test
        @DisplayName("Debe delegar en el repositorio el cambio de modo de notificación")
        fun `should change notification mode`() {
            useCase.changeNotificationMode(otherUserId, auctionId, NotificationMode.NONE)

            verify(favoriteAuctionRepository, times(1))
                .changeNotificationMode(otherUserId, auctionId, NotificationMode.NONE)
        }

        @Test
        @DisplayName("Debe propagar la excepción si la subasta no está en favoritos")
        fun `should propagate exception when auction is not a favorite`() {
            `when`(favoriteAuctionRepository.changeNotificationMode(otherUserId, auctionId, NotificationMode.ALL))
                .thenThrow(IllegalArgumentException("no está en favoritos"))

            assertThrows(IllegalArgumentException::class.java) {
                useCase.changeNotificationMode(otherUserId, auctionId, NotificationMode.ALL)
            }
        }
    }

    @Nested
    @DisplayName("getUserFavorites")
    inner class GetUserFavoritesTests {
        private val favoriteItem =
            FavoriteAuctionItem(
                auction =
                    AuctionListItem(
                        id = auctionId,
                        cardName = "Pikachu Illustrator",
                        cardImage = "pikachu.png",
                        currentBid = 100.0,
                        remainingDays = 3,
                        cardState = CardState.NEW,
                        cardCategory = "Pokémon",
                        cardCollection = null,
                        cardNumber = null,
                        status = AuctionStatus.IN_PROGRESS,
                    ),
                notificationMode = NotificationMode.ALL,
            )

        @Test
        @DisplayName("Debe devolver las subastas favoritas del usuario con su modo de notificación")
        fun `should return user favorite auctions with notification mode`() {
            `when`(favoriteAuctionRepository.getFavoritesByUserId(otherUserId))
                .thenReturn(listOf(favoriteItem))

            val result = useCase.getUserFavorites(otherUserId)

            assertEquals(1, result.size)
            assertEquals(auctionId, result[0].auction.id)
            assertEquals(NotificationMode.ALL, result[0].notificationMode)
            verify(favoriteAuctionRepository, times(1)).getFavoritesByUserId(otherUserId)
        }
    }
}
