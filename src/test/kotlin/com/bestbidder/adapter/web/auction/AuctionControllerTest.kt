package com.bestbidder.adapter.web.auction

import com.bestbidder.adapter.web.auction.dto.CreateAuctionRequest
import com.bestbidder.adapter.web.auction.dto.EditAuctionRequest
import com.bestbidder.adapter.web.exception.GlobalExceptionHandler
import com.bestbidder.domain.auction.Auction
import com.bestbidder.domain.auction.AuctionCreator
import com.bestbidder.domain.auction.AuctionDetail
import com.bestbidder.domain.auction.AuctionListItem
import com.bestbidder.domain.auction.AuctionStatus
import com.bestbidder.domain.auction.AuctionSummary
import com.bestbidder.domain.auction.FavoriteAuctionItem
import com.bestbidder.domain.auction.NotificationMode
import com.bestbidder.domain.card.Card
import com.bestbidder.domain.card.CardState
import com.bestbidder.infrastructure.security.AuthenticatedUser
import com.bestbidder.usecase.auction.AuctionSort
import com.bestbidder.usecase.auction.CreateAuctionUseCase
import com.bestbidder.usecase.auction.EditAuctionCommand
import com.bestbidder.usecase.auction.EditAuctionUseCase
import com.bestbidder.usecase.auction.GetAuctionUseCase
import com.bestbidder.usecase.auction.ManageFavoriteAuctionUseCase
import com.bestbidder.usecase.auction.RegisterAuctionViewUseCase
import com.bestbidder.usecase.port.CardRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Instant
import java.util.UUID

@WebMvcTest(AuctionController::class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler::class)
class AuctionControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var createAuctionUseCase: CreateAuctionUseCase

    @MockBean
    private lateinit var getAuctionUseCase: GetAuctionUseCase

    @MockBean
    private lateinit var cardRepository: CardRepository

    @MockBean
    private lateinit var manageFavoriteAuctionUseCase: ManageFavoriteAuctionUseCase

    @MockBean
    private lateinit var registerAuctionViewUseCase: RegisterAuctionViewUseCase

    @MockBean
    private lateinit var editAuctionUseCase: EditAuctionUseCase

    private val now = Instant.now()
    private val defaultUserId = UUID.randomUUID()
    private val sampleCardId = UUID.randomUUID()

    private val authToken =
        UsernamePasswordAuthenticationToken(
            AuthenticatedUser(defaultUserId, "paula"),
            null,
            emptyList(),
        )

    private val sampleCard =
        Card(
            id = sampleCardId,
            ownerId = defaultUserId,
            name = "Pikachu Illustrator",
            image = "pikachu.png",
            category = "Pokémon",
            state = CardState.NEW,
        )

    @Nested
    @DisplayName("POST /api/auction/create")
    inner class CreateAuctionTests {
        @Test
        @DisplayName("Debe retornar 201 Created cuando la creacion es exitosa")
        fun `should return 201 Created when auction creation is successful`() {
            val request =
                CreateAuctionRequest(
                    cardId = sampleCardId,
                    description = "Estado impecable",
                    minPrize = 500.0,
                    startedAt = now,
                    durationInDays = 3,
                )

            val createdAuction =
                Auction(
                    id = UUID.randomUUID(),
                    creatorId = defaultUserId,
                    creatorUsername = "paula",
                    card = sampleCard,
                    description = request.description,
                    minPrize = request.minPrize,
                    startedAt = request.startedAt,
                    durationInDays = request.durationInDays,
                )

            `when`(cardRepository.getById(sampleCardId)).thenReturn(sampleCard)
            `when`(
                createAuctionUseCase.create(
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                ),
            ).thenReturn(createdAuction)

            mockMvc.perform(
                post("/api/auction/create")
                    .with(authentication(authToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").value(createdAuction.id.toString()))
                .andExpect(jsonPath("$.creator_username").value("paula"))
                .andExpect(jsonPath("$.card.name").value("Pikachu Illustrator"))
                .andExpect(jsonPath("$.description").value("Estado impecable"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))

            verify(createAuctionUseCase, times(1)).create(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
            )
        }

        @Test
        @DisplayName("Debe retornar 400 Bad Request cuando la carta pertenece a otro usuario")
        fun `should return 400 Bad Request when card belongs to another user`() {
            val request =
                CreateAuctionRequest(
                    cardId = sampleCardId,
                    description = "Intento con carta ajena",
                    minPrize = 500.0,
                    startedAt = now,
                    durationInDays = 3,
                )

            val foreignCard = sampleCard.copy(ownerId = UUID.randomUUID())
            `when`(cardRepository.getById(sampleCardId)).thenReturn(foreignCard)

            mockMvc.perform(
                post("/api/auction/create")
                    .with(authentication(authToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            )
                .andExpect(status().isBadRequest)

            verify(createAuctionUseCase, never()).create(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
            )
        }

        @Test
        @DisplayName("Debe retornar 400 Bad Request cuando la carta no existe")
        fun `should return 400 Bad Request when card does not exist`() {
            val request =
                CreateAuctionRequest(
                    cardId = sampleCardId,
                    description = "Carta inexistente",
                    minPrize = 500.0,
                    startedAt = now,
                    durationInDays = 3,
                )

            `when`(cardRepository.getById(sampleCardId)).thenReturn(null)

            mockMvc.perform(
                post("/api/auction/create")
                    .with(authentication(authToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            )
                .andExpect(status().isBadRequest)

            verify(createAuctionUseCase, never()).create(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
            )
        }
    }

    @Nested
    @DisplayName("GET Endpoints")
    inner class GetAuctionTests {
        @Test
        @DisplayName("GET /api/auction/all - Debe retornar 200 OK con el listado completo")
        fun `should return 200 OK with all auctions`() {
            val summary =
                AuctionListItem(
                    id = UUID.randomUUID(),
                    cardName = "Pikachu Illustrator",
                    cardImage = "pikachu.png",
                    currentBid = 100.0,
                    remainingDays = 3,
                    cardState = CardState.NEW,
                    cardCategory = "Pokémon",
                    cardCollection = null,
                    cardNumber = 5,
                    status = AuctionStatus.UPCOMING,
                )

            `when`(getAuctionUseCase.getAll(AuctionSort.ENDING_SOON)).thenReturn(listOf(summary))

            mockMvc.perform(get("/api/auction/all"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(summary.id.toString()))
                .andExpect(jsonPath("$[0].cardName").value("Pikachu Illustrator"))
                .andExpect(jsonPath("$[0].cardImage").value("pikachu.png"))
                .andExpect(jsonPath("$[0].currentBid").value(100.0))
                .andExpect(jsonPath("$[0].remainingDays").value(3))
                .andExpect(jsonPath("$[0].cardState").value("NEW"))
                .andExpect(jsonPath("$[0].cardCategory").value("Pokémon"))
                .andExpect(jsonPath("$[0].cardNumber").value(5))
                .andExpect(jsonPath("$[0].status").value("UPCOMING"))
        }

        @Test
        @DisplayName("GET /api/auction/all?sort=PRICE_ASC - Debe pasar la ordenación al caso de uso")
        fun `should pass sort param to use case`() {
            `when`(getAuctionUseCase.getAll(AuctionSort.PRICE_ASC)).thenReturn(emptyList())

            mockMvc.perform(get("/api/auction/all").param("sort", "PRICE_ASC"))
                .andExpect(status().isOk)

            verify(getAuctionUseCase).getAll(AuctionSort.PRICE_ASC)
        }

        @Test
        @DisplayName("GET /api/auction/all?sort=INVALIDO - Debe retornar 400")
        fun `should return 400 when sort param is invalid`() {
            mockMvc.perform(get("/api/auction/all").param("sort", "INVALIDO"))
                .andExpect(status().isBadRequest)
        }
    }

    @Nested
    @DisplayName("GET /api/auction/user/my-auctions")
    inner class MyAuctionsTests {
        @Test
        @DisplayName("Debe retornar 200 OK con las subastas del usuario autenticado")
        fun `should return 200 OK with user auctions`() {
            val summary =
                AuctionSummary(
                    id = UUID.randomUUID(),
                    cardName = "Charizard",
                    cardImage = "charizard.png",
                    status = AuctionStatus.FINISHED,
                    minPrize = 10.0,
                    currentBid = 25.0,
                    startedAt = 1_700_000_000_000,
                    endsAt = 1_700_086_400_000,
                    winnerUsername = "ganador",
                )

            `when`(getAuctionUseCase.getUserAuctions(defaultUserId)).thenReturn(listOf(summary))

            mockMvc.perform(
                get("/api/auction/user/my-auctions")
                    .with(authentication(authToken)),
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(summary.id.toString()))
                .andExpect(jsonPath("$[0].cardName").value("Charizard"))
                .andExpect(jsonPath("$[0].cardImage").value("charizard.png"))
                .andExpect(jsonPath("$[0].status").value("FINISHED"))
                .andExpect(jsonPath("$[0].minPrize").value(10.0))
                .andExpect(jsonPath("$[0].currentBid").value(25.0))
                .andExpect(jsonPath("$[0].startedAt").value(1_700_000_000_000))
                .andExpect(jsonPath("$[0].endsAt").value(1_700_086_400_000))
                .andExpect(jsonPath("$[0].winnerUsername").value("ganador"))

            verify(getAuctionUseCase, times(1)).getUserAuctions(defaultUserId)
        }

        @Test
        @DisplayName("Debe retornar 200 OK y lista vacía si el usuario no tiene subastas")
        fun `should return 200 OK with empty list when user has no auctions`() {
            `when`(getAuctionUseCase.getUserAuctions(defaultUserId)).thenReturn(emptyList())

            mockMvc.perform(
                get("/api/auction/user/my-auctions")
                    .with(authentication(authToken)),
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.length()").value(0))
        }

        @Test
        @DisplayName("Debe devolver winner_username null si la subasta no tiene ganador")
        fun `should return null winner when auction has no winner`() {
            val summary =
                AuctionSummary(
                    id = UUID.randomUUID(),
                    cardName = "Pikachu",
                    cardImage = "pikachu.png",
                    status = AuctionStatus.IN_PROGRESS,
                    minPrize = 5.0,
                    currentBid = 5.0,
                    startedAt = 1_700_000_000_000,
                    endsAt = 1_700_086_400_000,
                    winnerUsername = null,
                )

            `when`(getAuctionUseCase.getUserAuctions(defaultUserId)).thenReturn(listOf(summary))

            mockMvc.perform(
                get("/api/auction/user/my-auctions")
                    .with(authentication(authToken)),
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$[0].status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$[0].winnerUsername").doesNotExist())
        }

        @Test
        @DisplayName("Debe devolver las subastas en los distintos estados")
        fun `should return auctions in every status`() {
            val statuses = AuctionStatus.values().toList()
            val summaries =
                statuses.map {
                    AuctionSummary(
                        id = UUID.randomUUID(),
                        cardName = "Carta $it",
                        cardImage = "carta.png",
                        status = it,
                        minPrize = 1.0,
                        currentBid = 1.0,
                        startedAt = 1_700_000_000_000,
                        endsAt = 1_700_086_400_000,
                        winnerUsername = null,
                    )
                }

            `when`(getAuctionUseCase.getUserAuctions(defaultUserId)).thenReturn(summaries)

            val result =
                mockMvc.perform(
                    get("/api/auction/user/my-auctions")
                        .with(authentication(authToken)),
                )
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.length()").value(statuses.size))

            statuses.forEachIndexed { index, s ->
                result.andExpect(jsonPath("$[$index].status").value(s.name))
            }
        }
    }

    @Nested
    @DisplayName("Favorite Auctions Endpoints")
    inner class FavoriteAuctionTests {
        @Test
        @DisplayName("POST /api/auction/{id}/favorite/{mode} - Debe retornar 201 Created")
        fun `should add favorite auction`() {
            val auctionId = UUID.randomUUID()

            mockMvc.perform(
                post("/api/auction/$auctionId/favorite/ALL")
                    .with(authentication(authToken)),
            )
                .andExpect(status().isCreated)

            verify(manageFavoriteAuctionUseCase, times(1))
                .addFavorite(defaultUserId, auctionId, NotificationMode.ALL)
        }

        @Test
        @DisplayName("POST /api/auction/{id}/favorite/{mode} - Debe retornar 400 si el caso de uso lo rechaza")
        fun `should return 400 Bad Request when adding favorite fails`() {
            val auctionId = UUID.randomUUID()

            doThrow(IllegalArgumentException("No puedes añadir tu propia subasta a favoritos"))
                .`when`(manageFavoriteAuctionUseCase)
                .addFavorite(defaultUserId, auctionId, NotificationMode.BASIC)

            mockMvc.perform(
                post("/api/auction/$auctionId/favorite/BASIC")
                    .with(authentication(authToken)),
            )
                .andExpect(status().isBadRequest)
        }

        @Test
        @DisplayName("POST /api/auction/{id}/favorite/{mode} - Debe retornar 400 si el modo no es válido")
        fun `should return 400 Bad Request when notification mode is invalid`() {
            val auctionId = UUID.randomUUID()

            mockMvc.perform(
                post("/api/auction/$auctionId/favorite/INVALIDO")
                    .with(authentication(authToken)),
            )
                .andExpect(status().isBadRequest)

            verify(manageFavoriteAuctionUseCase, never())
                .addFavorite(defaultUserId, auctionId, NotificationMode.BASIC)
        }

        @Test
        @DisplayName("PATCH /api/auction/{id}/favorite/{mode} - Debe retornar 204 No Content")
        fun `should change notification mode`() {
            val auctionId = UUID.randomUUID()

            mockMvc.perform(
                patch("/api/auction/$auctionId/favorite/NONE")
                    .with(authentication(authToken)),
            )
                .andExpect(status().isNoContent)

            verify(manageFavoriteAuctionUseCase, times(1))
                .changeNotificationMode(defaultUserId, auctionId, NotificationMode.NONE)
        }

        @Test
        @DisplayName("PATCH /api/auction/{id}/favorite/{mode} - Debe retornar 400 si la subasta no es favorita")
        fun `should return 400 Bad Request when changing mode of a non favorite auction`() {
            val auctionId = UUID.randomUUID()

            doThrow(IllegalArgumentException("La subasta $auctionId no está en favoritos"))
                .`when`(manageFavoriteAuctionUseCase)
                .changeNotificationMode(defaultUserId, auctionId, NotificationMode.ALL)

            mockMvc.perform(
                patch("/api/auction/$auctionId/favorite/ALL")
                    .with(authentication(authToken)),
            )
                .andExpect(status().isBadRequest)
        }

        @Test
        @DisplayName("DELETE /api/auction/{id}/favorite - Debe retornar 204 No Content")
        fun `should remove favorite auction`() {
            val auctionId = UUID.randomUUID()

            mockMvc.perform(
                delete("/api/auction/$auctionId/favorite")
                    .with(authentication(authToken)),
            )
                .andExpect(status().isNoContent)

            verify(manageFavoriteAuctionUseCase, times(1)).removeFavorite(defaultUserId, auctionId)
        }

        @Test
        @DisplayName("GET /api/auction/favorites - Debe retornar 200 OK con las favoritas y su modo de notificación")
        fun `should list favorite auctions with notification mode`() {
            val favorite =
                FavoriteAuctionItem(
                    auction =
                        AuctionListItem(
                            id = UUID.randomUUID(),
                            cardName = "Charizard",
                            cardImage = "charizard.png",
                            currentBid = 150.0,
                            remainingDays = 2,
                            cardState = CardState.NEAR_MINT,
                            cardCategory = "Pokémon",
                            cardCollection = "Base Set",
                            cardNumber = 4,
                            status = AuctionStatus.IN_PROGRESS,
                        ),
                    notificationMode = NotificationMode.ALL,
                )

            `when`(manageFavoriteAuctionUseCase.getUserFavorites(defaultUserId))
                .thenReturn(listOf(favorite))

            mockMvc.perform(
                get("/api/auction/favorites")
                    .with(authentication(authToken)),
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(favorite.auction.id.toString()))
                .andExpect(jsonPath("$[0].cardName").value("Charizard"))
                .andExpect(jsonPath("$[0].cardImage").value("charizard.png"))
                .andExpect(jsonPath("$[0].currentBid").value(150.0))
                .andExpect(jsonPath("$[0].remainingDays").value(2))
                .andExpect(jsonPath("$[0].cardState").value("NEAR_MINT"))
                .andExpect(jsonPath("$[0].cardCategory").value("Pokémon"))
                .andExpect(jsonPath("$[0].cardCollection").value("Base Set"))
                .andExpect(jsonPath("$[0].cardNumber").value(4))
                .andExpect(jsonPath("$[0].status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$[0].notificationMode").value("ALL"))
        }

        @Test
        @DisplayName("GET /api/auction/favorites - Debe retornar lista vacía si no tiene favoritas")
        fun `should return empty list when user has no favorites`() {
            `when`(manageFavoriteAuctionUseCase.getUserFavorites(defaultUserId)).thenReturn(emptyList())

            mockMvc.perform(
                get("/api/auction/favorites")
                    .with(authentication(authToken)),
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.length()").value(0))
        }
    }

    @Nested
    @DisplayName("GET /api/auction/{id}")
    inner class AuctionDetailTests {
        private fun buildDetail(
            creator: AuctionCreator?,
            isFavorite: Boolean?,
        ) = AuctionDetail(
            id = UUID.randomUUID(),
            status = AuctionStatus.IN_PROGRESS,
            cardName = "Pikachu Illustrator",
            cardImage = "pikachu.png",
            cardCollection = "Promo",
            cardNumber = 5,
            cardState = CardState.NEW,
            minPrize = 100.0,
            increment = 1.0,
            lastBid = 150.0,
            bidCount = 3,
            startedAt = now,
            endsAt = now.plusSeconds(3600),
            description = "Estado impecable",
            creator = creator,
            isFavorite = isFavorite,
        )

        @Test
        @DisplayName("Debe retornar 200 con creador e isFavorite si lo consulta otro usuario")
        fun `should return detail with creator and favorite`() {
            val creator = AuctionCreator(UUID.randomUUID(), "user2", 4)
            val detail = buildDetail(creator, true)
            `when`(getAuctionUseCase.getDetail(detail.id, defaultUserId)).thenReturn(detail)

            mockMvc.perform(get("/api/auction/${detail.id}").with(authentication(authToken)))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(detail.id.toString()))
                .andExpect(jsonPath("$.cardName").value("Pikachu Illustrator"))
                .andExpect(jsonPath("$.lastBid").value(150.0))
                .andExpect(jsonPath("$.bidCount").value(3))
                .andExpect(jsonPath("$.creator.username").value("user2"))
                .andExpect(jsonPath("$.creator.avatarNum").value(4))
                .andExpect(jsonPath("$.isFavorite").value(true))
        }

        @Test
        @DisplayName("Debe retornar creador e isFavorite null si quien consulta es el propietario")
        fun `should return null creator and favorite for owner`() {
            val detail = buildDetail(creator = null, isFavorite = null)
            `when`(getAuctionUseCase.getDetail(detail.id, defaultUserId)).thenReturn(detail)

            mockMvc.perform(get("/api/auction/${detail.id}").with(authentication(authToken)))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.creator").doesNotExist())
                .andExpect(jsonPath("$.isFavorite").doesNotExist())
        }

        @Test
        @DisplayName("Debe llamar al caso de uso con viewerId null si no hay usuario autenticado")
        fun `should call use case with null viewer when anonymous`() {
            val detail = buildDetail(AuctionCreator(UUID.randomUUID(), "user2", null), null)
            `when`(getAuctionUseCase.getDetail(detail.id, null)).thenReturn(detail)

            mockMvc.perform(get("/api/auction/${detail.id}"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.creator.username").value("user2"))
                .andExpect(jsonPath("$.isFavorite").doesNotExist())

            verify(getAuctionUseCase, times(1)).getDetail(detail.id, null)
        }

        @Test
        @DisplayName("Debe retornar 404 si la subasta no existe")
        fun `should return 404 when auction does not exist`() {
            val id = UUID.randomUUID()
            `when`(getAuctionUseCase.getDetail(id, defaultUserId)).thenReturn(null)

            mockMvc.perform(get("/api/auction/$id").with(authentication(authToken)))
                .andExpect(status().isNotFound)
        }

        @Test
        @DisplayName("Registra la visita cuando consulta otro usuario")
        fun `should register view when viewer is not owner`() {
            val detail = buildDetail(AuctionCreator(UUID.randomUUID(), "user2", 4), false)
            `when`(getAuctionUseCase.getDetail(detail.id, defaultUserId)).thenReturn(detail)

            mockMvc.perform(get("/api/auction/${detail.id}").with(authentication(authToken)))
                .andExpect(status().isOk)

            verify(registerAuctionViewUseCase, times(1)).register(detail.id, defaultUserId)
        }

        @Test
        @DisplayName("No registra la visita del propietario")
        fun `should not register view when viewer is owner`() {
            val detail = buildDetail(creator = null, isFavorite = null)
            `when`(getAuctionUseCase.getDetail(detail.id, defaultUserId)).thenReturn(detail)

            mockMvc.perform(get("/api/auction/${detail.id}").with(authentication(authToken)))
                .andExpect(status().isOk)

            verify(registerAuctionViewUseCase, never()).register(detail.id, defaultUserId)
        }

        @Test
        @DisplayName("No registra nada si la subasta no existe")
        fun `should not register view when auction not found`() {
            val id = UUID.randomUUID()
            `when`(getAuctionUseCase.getDetail(id, defaultUserId)).thenReturn(null)

            mockMvc.perform(get("/api/auction/$id").with(authentication(authToken)))
                .andExpect(status().isNotFound)

            verify(registerAuctionViewUseCase, never()).register(id, defaultUserId)
        }
    }

    @Nested
    @DisplayName("PATCH /api/auction/{id}")
    inner class EditAuctionTests {
        @Test
        @DisplayName("Debe retornar 200 OK cuando la edición es exitosa")
        fun `should return 200 OK when auction edit is successful`() {
            val auctionId = UUID.randomUUID()
            val newStart = now.plusSeconds(7200)
            val request =
                EditAuctionRequest(
                    description = "Descripción actualizada",
                    minPrize = 750.0,
                    startedAt = newStart,
                    durationInDays = 5,
                )

            val updatedAuction =
                Auction(
                    id = auctionId,
                    creatorId = defaultUserId,
                    creatorUsername = "paula",
                    card = sampleCard,
                    description = request.description,
                    minPrize = request.minPrize!!,
                    startedAt = request.startedAt!!,
                    durationInDays = request.durationInDays!!,
                )

            `when`(
                editAuctionUseCase.edit(
                    EditAuctionCommand(
                        auctionId = auctionId,
                        requesterUserId = defaultUserId,
                        description = request.description,
                        minPrize = request.minPrize,
                        startedAt = request.startedAt,
                        durationInDays = request.durationInDays,
                    ),
                ),
            ).thenReturn(updatedAuction)

            mockMvc.perform(
                patch("/api/auction/$auctionId")
                    .with(authentication(authToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(auctionId.toString()))
                .andExpect(jsonPath("$.description").value("Descripción actualizada"))
                .andExpect(jsonPath("$.min_prize").value(750.0))
                .andExpect(jsonPath("$.durationInDays").value(5))

            verify(editAuctionUseCase, times(1)).edit(
                EditAuctionCommand(
                    auctionId = auctionId,
                    requesterUserId = defaultUserId,
                    description = request.description,
                    minPrize = request.minPrize,
                    startedAt = request.startedAt,
                    durationInDays = request.durationInDays,
                ),
            )
        }

        @Test
        @DisplayName("Debe retornar 400 Bad Request cuando el caso de uso lanza IllegalArgumentException")
        fun `should return 400 Bad Request when edit fails with IllegalArgumentException`() {
            val auctionId = UUID.randomUUID()
            val request = EditAuctionRequest(minPrize = 0.0)

            `when`(editAuctionUseCase.edit(any()))
                .thenThrow(IllegalArgumentException("El precio mínimo debe ser mayor que 0"))

            mockMvc.perform(
                patch("/api/auction/$auctionId")
                    .with(authentication(authToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            )
                .andExpect(status().isBadRequest)
        }

        @Test
        @DisplayName("Debe retornar 409 Conflict cuando la subasta ya ha comenzado")
        fun `should return 409 Conflict when auction has already started`() {
            val auctionId = UUID.randomUUID()
            val request = EditAuctionRequest(description = "Tarde")

            `when`(editAuctionUseCase.edit(any()))
                .thenThrow(IllegalStateException("Solo se pueden editar subastas que aún no han empezado"))

            mockMvc.perform(
                patch("/api/auction/$auctionId")
                    .with(authentication(authToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            )
                .andExpect(status().isConflict)
        }
    }
}
