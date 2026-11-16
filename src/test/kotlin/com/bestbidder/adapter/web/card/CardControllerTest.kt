package com.bestbidder.adapter.web.card

import com.bestbidder.adapter.web.card.dto.CreateCardRequest
import com.bestbidder.adapter.web.card.dto.UpdateCardRequest
import com.bestbidder.adapter.web.exception.GlobalExceptionHandler
import com.bestbidder.domain.card.Card
import com.bestbidder.domain.card.CardState
import com.bestbidder.infrastructure.security.AuthenticatedUser
import com.bestbidder.usecase.card.CreateCardUseCase
import com.bestbidder.usecase.card.DeleteCardUseCase
import com.bestbidder.usecase.card.GetCardUseCase
import com.bestbidder.usecase.card.UpdateCardUseCase
import com.bestbidder.usecase.port.FileStoragePort
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.any
import org.mockito.Mockito.eq
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.mock.web.MockMultipartFile
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
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

    @MockBean
    private lateinit var deleteCardUseCase: DeleteCardUseCase

    @MockBean
    private lateinit var updateCardUseCase: UpdateCardUseCase

    @MockBean
    private lateinit var fileStoragePort: FileStoragePort

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
        @Suppress("LongMethod")
        fun `should return 201 Created when card creation is successful`() {
            setAuthenticatedUser(userId, "user4")

            // DTO de la imagen que va a aparte
            val request =
                CreateCardRequest(
                    name = "Rayquaza Gold Star",
                    category = "Pokemon",
                    collection = "Evoluciones de Paldea",
                    num = 107,
                    state = CardState.NEW,
                    description = "Recién graduada PSA 10",
                )

            // Simular la subida
            val mockFile =
                MockMultipartFile(
                    "image",
                    "rayquaza.png",
                    "image/png",
                    "fake-image-bytes".toByteArray(),
                )
            val mockImageUrl = "/uploads/rayquaza.png"

            // Simular lo que devuelve el caso de uso y el FileStorage
            `when`(fileStoragePort.store(any())).thenReturn(mockImageUrl)

            val createdCard =
                Card(
                    id = cardId,
                    ownerId = userId,
                    name = request.name,
                    image = mockImageUrl,
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
                    image = mockImageUrl,
                    category = request.category,
                    collection = request.collection,
                    num = request.num,
                    state = request.state,
                    description = request.description,
                ),
            ).thenReturn(createdCard)

            // petición multipart
            val dataPart =
                MockMultipartFile(
                    "data",
                    "",
                    "application/json",
                    objectMapper.writeValueAsBytes(request),
                )

            mockMvc.perform(
                multipart("/api/card/create")
                    .file(dataPart)
                    .file(mockFile)
                    .principal(
                        UsernamePasswordAuthenticationToken(
                            AuthenticatedUser(userId, "user4"),
                            null,
                            emptyList(),
                        ),
                    ),
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").value(cardId.toString()))
                .andExpect(jsonPath("$.name").value("Rayquaza Gold Star"))
                .andExpect(jsonPath("$.image").value(mockImageUrl))

            verify(createCardUseCase, times(1)).execute(
                ownerId = userId,
                name = request.name,
                image = mockImageUrl,
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

    @Nested
    @DisplayName("PUT /api/card/{id}")
    inner class UpdateCardTests {
        @Test
        @DisplayName("Debe retornar 200 OK al editar una carta correctamente")
        @Suppress("LongMethod")
        fun `should return 200 OK when update is successful`() {
            setAuthenticatedUser(userId, "user4")

            val request =
                UpdateCardRequest(
                    name = "Rayquaza Editado",
                    state = CardState.LIGHT_PLAYED,
                )

            val existingCard =
                Card(
                    id = cardId,
                    ownerId = userId,
                    name = "Rayquaza Original",
                    image = "/uploads/old-image.png",
                    category = "Pokemon",
                    collection = "Evoluciones de Paldea",
                    num = 107,
                    state = CardState.NEW,
                    description = "Descripción vieja",
                )

            val mockFile =
                MockMultipartFile(
                    "image",
                    "new-rayquaza.png",
                    "image/png",
                    "fake-new-image-bytes".toByteArray(),
                )
            val newImageUrl = "/uploads/new-rayquaza.png"

            `when`(getCardUseCase.getById(cardId)).thenReturn(existingCard)
            `when`(fileStoragePort.store(any())).thenReturn(newImageUrl)

            val updatedCard =
                existingCard.copy(
                    name = request.name!!,
                    state = request.state!!,
                    image = newImageUrl,
                )

            `when`(updateCardUseCase.execute(any(), any(), any())).thenReturn(updatedCard)

            val dataPart =
                MockMultipartFile(
                    "data",
                    "",
                    "application/json",
                    objectMapper.writeValueAsBytes(request),
                )

            mockMvc.perform(
                multipart("/api/card/{id}", cardId)
                    .file(dataPart)
                    .file(mockFile)
                    // TRUCO: multipart() crea POST por defecto, lo forzamos a PUT
                    .with { req ->
                        req.method = "PUT"
                        req
                    }
                    .principal(
                        UsernamePasswordAuthenticationToken(
                            AuthenticatedUser(userId, "user4"),
                            null,
                            emptyList(),
                        ),
                    ),
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.name").value("Rayquaza Editado"))
                .andExpect(jsonPath("$.image").value(newImageUrl))

            verify(updateCardUseCase, times(1)).execute(eq(cardId), eq(userId), any())
        }
    }

    @Nested
    @DisplayName("DELETE /api/card/{id}")
    inner class DeleteCardTests {
        @Test
        @DisplayName("Debe retornar 204 No Content al borrar una carta")
        fun `should return 204 No Content when delete is successful`() {
            setAuthenticatedUser(userId, "user4")

            mockMvc.perform(
                delete("/api/card/{id}", cardId)
                    .principal(
                        UsernamePasswordAuthenticationToken(
                            AuthenticatedUser(userId, "user4"),
                            null,
                            emptyList(),
                        ),
                    ),
            )
                .andExpect(status().isNoContent)

            verify(deleteCardUseCase, times(1)).execute(cardId, userId)
        }
    }
}
