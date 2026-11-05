package com.bestbidder.adapter.web.user

import com.bestbidder.adapter.web.exception.GlobalExceptionHandler
import com.bestbidder.adapter.web.user.dto.LoginRequest
import com.bestbidder.adapter.web.user.dto.RegisterRequest
import com.bestbidder.domain.user.User
import com.bestbidder.usecase.user.AuthResult
import com.bestbidder.usecase.user.AuthUseCase
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

@WebMvcTest(AuthController::class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler::class)
class AuthControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var authUseCase: AuthUseCase

    @MockBean
    private lateinit var jwtService: com.bestbidder.infrastructure.config.JwtService

    @Nested
    @DisplayName("POST /api/auth/register")
    inner class RegisterTests {
        @Test
        @DisplayName("Debe retornar 201 Created cuando el registro es exitoso")
        fun `should return 201 on successful registration`() {
            val request = RegisterRequest("david@test.com", "david_85", "secreto85")
            val createdUser =
                User(
                    id = UUID.randomUUID(),
                    email = request.email,
                    username = request.username,
                    passwordHash = "hashed",
                    description = null,
                    avatarNum = null,
                )

            `when`(authUseCase.register(request.email, request.username, request.password))
                .thenReturn(createdUser)

            mockMvc.perform(
                post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.email").value("david@test.com"))
                .andExpect(jsonPath("$.username").value("david_85"))
        }

        @Test
        @DisplayName("Debe retornar 400 si el formato del email es inválido")
        fun `should return 400 on invalid email format`() {
            val request = RegisterRequest("email-invalido", "david_85", "secreto85")

            mockMvc.perform(
                post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            )
                .andExpect(status().isBadRequest)
        }
    }

    @Nested
    @DisplayName("POST /api/auth/login")
    inner class LoginTests {
        @Test
        @DisplayName("Debe retornar 200 OK y los tokens al hacer login")
        fun `should return 200 with tokens on successful login`() {
            val request = LoginRequest("david_85", "secreto85")
            val mockUser =
                User(
                    id = UUID.randomUUID(),
                    email = "david@test.com",
                    username = "david_85",
                    passwordHash = "hashed",
                    description = null,
                    avatarNum = null,
                )
            val authResult =
                AuthResult(
                    mockUser,
                    "fake_access_token",
                    "fake_refresh_token",
                )

            `when`(authUseCase.login(request.identifier, request.password))
                .thenReturn(authResult)

            mockMvc.perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.accessToken").value("fake_access_token"))
                .andExpect(jsonPath("$.refreshToken").value("fake_refresh_token"))
                .andExpect(jsonPath("$.user.username").value("david_85"))
        }
    }
}
