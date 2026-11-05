package com.bestbidder.adapter.web.card

import com.bestbidder.adapter.web.card.dto.CreateCardRequest
import com.bestbidder.adapter.web.exception.GlobalExceptionHandler
import com.bestbidder.domain.card.Card
import com.bestbidder.domain.card.CardState
import com.bestbidder.infrastructure.security.AuthenticatedUser
import com.bestbidder.usecase.card.CreateCardUseCase
import com.bestbidder.usecase.card.GetCardUseCase
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

@WebMvcTest(CardController::class)
@Import(GlobalExceptionHandler::class)
class CardControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var createCardUseCase: CreateCardUseCase

    @MockBean
    private lateinit var getCardUseCase: GetCardUseCase

    private val userId = UUID.randomUUID()
    private val cardId = UUID.randomUUID()

    private fun setAuthenticatedUser(
        userId: UUID,
        username: String,
    ) {
        val principal = AuthenticatedUser(id = userId, username = username)
        val auth = UsernamePasswordAuthenticationToken(principal, null, emptyList())
        SecurityContextHolder.getContext().authentication = auth
    }

    @Nested
    @DisplayName("POST /api/card/create")
    inner class CreateCardTests {
        @Test
        @DisplayName("Debe retornar 201 Created al crear una carta de forma exitosa")
        fun `should return 201 Created when card creation is successful`() {
            setAuthenticatedUser(userId, "user4")

            val request =
                CreateCardRequest(
                    name = "Rayquaza Gold Star",
                    image = "rayquaza_gold.png",
                    category = "Pokémon",
                    collection = "EX Deoxys",
                    num = 107,
                    state = CardState.NEW,
                    description = "Recién graduada PSA 10",
                )

            val createdCard =
                Card(
                    id = cardId,
                    ownerId = userId,
                    name = request.name,
                    image = request.image,
                    category = request.category,
                    collection = request.collection,
                    num = request.num,
                    state = request.state,
                    description = request.description,
                )

            `when`(
                createCardUseCase.execute(
                    ownerId = userId,
                    name = request.name,
                    image = request.image,
                    category = request.category,
                    collection = request.collection,
                    num = request.num,
                    state = request.state,
                    description = request.description,
                ),
            ).thenReturn(createdCard)

            mockMvc.perform(
                post("/api/card/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").value(cardId.toString()))
                .andExpect(jsonPath("$.ownerId").value(userId.toString()))
                .andExpect(jsonPath("$.name").value("Rayquaza Gold Star"))
                .andExpect(jsonPath("$.state").value("NEW"))

            verify(createCardUseCase, times(1)).execute(
                ownerId = userId,
                name = request.name,
                image = request.image,
                category = request.category,
                collection = request.collection,
                num = request.num,
                state = request.state,
                description = request.description,
            )
        }
    }

    @Nested
    @DisplayName("GET Endpoints")
    inner class GetCardTests {
        @Test
        @DisplayName("GET /api/card/all - Debe retornar 200 OK con todas las cartas")
        fun `should return 200 OK with all cards`() {
            val card =
                Card(
                    id = cardId,
                    ownerId = userId,
                    name = "Charizard 1st Edition",
                    image = "charizard.png",
                    category = "Pokémon",
                    state = CardState.NEW,
                )

            `when`(getCardUseCase.getAll()).thenReturn(listOf(card))

            mockMvc.perform(get("/api/card/all"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Charizard 1st Edition"))
        }
    }
}
