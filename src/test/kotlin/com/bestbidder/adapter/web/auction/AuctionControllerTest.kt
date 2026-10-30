package com.bestbidder.adapter.web.auction

import com.bestbidder.adapter.web.auction.dto.CreateAuctionRequest
import com.bestbidder.adapter.web.exception.GlobalExceptionHandler
import com.bestbidder.domain.auction.Auction
import com.bestbidder.usecase.auction.CreateAuctionUseCase
import com.bestbidder.usecase.auction.GetAuctionUseCase
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Instant
import java.util.UUID

@WebMvcTest(AuctionController::class)
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

    private val now = Instant.now()
    private val defaultUserId = UUID.randomUUID()

    @Nested
    @DisplayName("POST /api/auction/create")
    inner class CreateAuctionTests {
        @Test
        @DisplayName("Debe retornar 201 Created cuando la creacion es exitosa")
        fun `should return 201 Created when auction creation is successful`() {
            val request =
                CreateAuctionRequest(
                    creator_id = defaultUserId,
                    creator_username = "paula",
                    card = "Pikachu Illustrator",
                    collection = "Promo",
                    min_prize = 500.0,
                    started_at = now,
                    duration_in_days = 3,
                )

            val createdAuction =
                Auction(
                    id = UUID.randomUUID(),
                    creator_id = request.creator_id,
                    creator_username = request.creator_username,
                    card = request.card,
                    collection = request.collection,
                    min_prize = request.min_prize,
                    startedAt = request.started_at,
                    durationInDays = request.duration_in_days,
                )

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
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").value(createdAuction.id.toString()))
                .andExpect(jsonPath("$.creator_username").value("paula"))
                .andExpect(jsonPath("$.card").value("Pikachu Illustrator"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))

            verify(createAuctionUseCase, times(1)).create(any(), any(), any(), any(), any(), any(), any())
        }

        @Test
        @DisplayName("Debe retornar 400 Bad Request cuando la subasta es duplicada")
        fun `should return 400 Bad Request cuando la subasta es duplicada`() {
            val request =
                CreateAuctionRequest(
                    creator_id = defaultUserId,
                    creator_username = "paula",
                    card = "Charizard 1st Edition",
                    collection = "Base Set",
                    min_prize = 150.0,
                    started_at = now,
                    duration_in_days = 3,
                )

            val errorMessage =
                "Ya existe una subasta activa o próxima de la carta 'Charizard 1st Edition' para el usuario 'paula'"

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
            ).thenThrow(IllegalArgumentException(errorMessage))

            mockMvc.perform(
                post("/api/auction/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(errorMessage))
        }

        @Test
        @DisplayName("Debe retornar 400 Bad Request cuando la duracion es invalida")
        fun `should return 400 Bad Request cuando la duracion es invalida`() {
            val request =
                CreateAuctionRequest(
                    creator_id = defaultUserId,
                    creator_username = "paula",
                    card = "Mewtwo",
                    collection = "Promo",
                    min_prize = 50.0,
                    started_at = now,
                    duration_in_days = 10,
                )

            val errorMessage = "La duración de la subasta debe estar entre 1 y 7 días"

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
            ).thenThrow(IllegalArgumentException(errorMessage))

            mockMvc.perform(
                post("/api/auction/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(errorMessage))
        }
    }

    @Nested
    @DisplayName("GET Endpoints")
    inner class GetAuctionTests {
        @Test
        @DisplayName("GET /api/auction/all - Debe retornar 200 OK con el listado completo")
        fun `should return 200 OK with all auctions`() {
            val auction1 =
                Auction(
                    creator_id = defaultUserId,
                    creator_username = "paula",
                    card = "Charizard",
                    collection = "Base Set",
                    min_prize = 100.0,
                    startedAt = now,
                    durationInDays = 3,
                )
            val auction2 =
                Auction(
                    creator_id = UUID.randomUUID(),
                    creator_username = "ash",
                    card = "Pikachu",
                    collection = "Yellow",
                    min_prize = 20.0,
                    startedAt = now.plusSeconds(3600),
                    durationInDays = 1,
                )

            `when`(getAuctionUseCase.getAll()).thenReturn(listOf(auction1, auction2))

            mockMvc.perform(get("/api/auction/all"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].creator_username").value("paula"))
                .andExpect(jsonPath("$[0].status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$[1].creator_username").value("ash"))
                .andExpect(jsonPath("$[1].status").value("UPCOMING"))
        }

        @Test
        @DisplayName("GET /api/auction/active - Debe retornar 200 OK con activas o proximas")
        fun `should return 200 OK with active auctions`() {
            val activeAuction =
                Auction(
                    creator_id = defaultUserId,
                    creator_username = "paula",
                    card = "Charizard",
                    collection = "Base Set",
                    min_prize = 100.0,
                    startedAt = now,
                    durationInDays = 3,
                )

            `when`(getAuctionUseCase.getActive()).thenReturn(listOf(activeAuction))

            mockMvc.perform(get("/api/auction/active"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].card").value("Charizard"))
                .andExpect(jsonPath("$[0].status").value("IN_PROGRESS"))
        }
    }
}
