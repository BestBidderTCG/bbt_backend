package com.bestbidder.adapter.web.auction

import com.bestbidder.adapter.web.auction.dto.CreateAuctionRequest
import com.bestbidder.adapter.web.exception.GlobalExceptionHandler
import com.bestbidder.domain.auction.Auction
import com.bestbidder.domain.card.Card
import com.bestbidder.domain.card.CardState
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
    private val sampleCardId = UUID.randomUUID()

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
                    creator_id = defaultUserId,
                    creator_username = "paula",
                    card_id = sampleCardId,
                    description = "Estado impecable",
                    min_prize = 500.0,
                    started_at = now,
                    duration_in_days = 3,
                )

            val createdAuction =
                Auction(
                    id = UUID.randomUUID(),
                    creator_id = request.creator_id,
                    creator_username = request.creator_username,
                    card = sampleCard,
                    description = request.description,
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
                    card = sampleCard,
                    description = null,
                    min_prize = 100.0,
                    startedAt = now,
                    durationInDays = 3,
                )

            `when`(getAuctionUseCase.getAll()).thenReturn(listOf(auction1))

            mockMvc.perform(get("/api/auction/all"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].creator_username").value("paula"))
                .andExpect(jsonPath("$[0].status").value("IN_PROGRESS"))
        }
    }
}
