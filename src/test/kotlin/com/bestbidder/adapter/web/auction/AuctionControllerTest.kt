package com.bestbidder.adapter.web.auction

import com.bestbidder.adapter.web.auction.dto.CreateAuctionRequest
import com.bestbidder.adapter.web.exception.GlobalExceptionHandler
import com.bestbidder.domain.auction.Auction
import com.bestbidder.domain.auction.AuctionStatus
import com.bestbidder.domain.auction.AuctionSummary
import com.bestbidder.domain.auction.UserAuctionSummary
import com.bestbidder.domain.card.Card
import com.bestbidder.domain.card.CardState
import com.bestbidder.infrastructure.security.AuthenticatedUser
import com.bestbidder.usecase.auction.AuctionSort
import com.bestbidder.usecase.auction.CreateAuctionUseCase
import com.bestbidder.usecase.auction.GetAuctionUseCase
import com.bestbidder.usecase.auction.ManageFavoriteAuctionUseCase
import com.bestbidder.usecase.port.CardRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
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
                AuctionSummary(
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
                .andExpect(jsonPath("$[0].card_name").value("Pikachu Illustrator"))
                .andExpect(jsonPath("$[0].card_image").value("pikachu.png"))
                .andExpect(jsonPath("$[0].current_bid").value(100.0))
                .andExpect(jsonPath("$[0].remaining_days").value(3))
                .andExpect(jsonPath("$[0].card_state").value("NEW"))
                .andExpect(jsonPath("$[0].card_category").value("Pokémon"))
                .andExpect(jsonPath("$[0].card_number").value(5))
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
                UserAuctionSummary(
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
                .andExpect(jsonPath("$[0].card_name").value("Charizard"))
                .andExpect(jsonPath("$[0].card_image").value("charizard.png"))
                .andExpect(jsonPath("$[0].status").value("FINISHED"))
                .andExpect(jsonPath("$[0].min_prize").value(10.0))
                .andExpect(jsonPath("$[0].current_bid").value(25.0))
                .andExpect(jsonPath("$[0].started_at").value(1_700_000_000_000))
                .andExpect(jsonPath("$[0].ends_at").value(1_700_086_400_000))
                .andExpect(jsonPath("$[0].winner_username").value("ganador"))

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
                UserAuctionSummary(
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
                .andExpect(jsonPath("$[0].winner_username").doesNotExist())
        }

        @Test
        @DisplayName("Debe devolver las subastas en los distintos estados")
        fun `should return auctions in every status`() {
            val statuses = AuctionStatus.values().toList()
            val summaries =
                statuses.map {
                    UserAuctionSummary(
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
        @DisplayName("POST /api/auction/{id}/favorite - Debe retornar 201 Created")
        fun `should add favorite auction`() {
            val auctionId = UUID.randomUUID()

            mockMvc.perform(
                post("/api/auction/$auctionId/favorite")
                    .with(authentication(authToken)),
            )
                .andExpect(status().isCreated)

            verify(manageFavoriteAuctionUseCase, times(1)).addFavorite(defaultUserId, auctionId)
        }

        @Test
        @DisplayName("POST /api/auction/{id}/favorite - Debe retornar 400 Bad Request si el caso de uso lo rechaza")
        fun `should return 400 Bad Request when adding favorite fails`() {
            val auctionId = UUID.randomUUID()

            `when`(manageFavoriteAuctionUseCase.addFavorite(defaultUserId, auctionId))
                .thenThrow(IllegalArgumentException("No puedes añadir tu propia subasta a favoritos"))

            mockMvc.perform(
                post("/api/auction/$auctionId/favorite")
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
        @DisplayName("GET /api/auction/favorites - Debe retornar 200 OK con las favoritas")
        fun `should list favorite auctions`() {
            val favoriteAuction =
                Auction(
                    creatorId = UUID.randomUUID(),
                    creatorUsername = "user2",
                    card = sampleCard,
                    minPrize = 150.0,
                    startedAt = now,
                    durationInDays = 2,
                )

            `when`(manageFavoriteAuctionUseCase.getUserFavorites(defaultUserId))
                .thenReturn(listOf(favoriteAuction))

            mockMvc.perform(
                get("/api/auction/favorites")
                    .with(authentication(authToken)),
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].creator_username").value("user2"))
        }
    }
}
