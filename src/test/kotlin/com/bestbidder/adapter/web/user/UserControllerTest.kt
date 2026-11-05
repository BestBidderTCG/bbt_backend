package com.bestbidder.adapter.web.user

import com.bestbidder.adapter.web.exception.GlobalExceptionHandler
import com.bestbidder.adapter.web.user.dto.UpdateUserProfileRequest
import com.bestbidder.domain.user.User
import com.bestbidder.infrastructure.security.AuthenticatedUser
import com.bestbidder.usecase.user.ChangePasswordUseCase
import com.bestbidder.usecase.user.GetUserUseCase
import com.bestbidder.usecase.user.UpdateUserUseCase
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

@WebMvcTest(UserController::class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler::class)
class UserControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var getUserUseCase: GetUserUseCase

    @MockBean
    private lateinit var updateUserUseCase: UpdateUserUseCase

    @MockBean
    private lateinit var changePasswordUseCase: ChangePasswordUseCase

    @MockBean
    private lateinit var jwtService: com.bestbidder.infrastructure.config.JwtService

    private val userId = UUID.randomUUID()
    private val mockUser =
        User(
            id = userId,
            email = "david@test.com",
            username = "david_85",
            passwordHash = "hashed",
            description = "Hola",
            avatarNum = 1,
        )

    private val authUser = AuthenticatedUser(userId, "david_85")
    private val authToken = UsernamePasswordAuthenticationToken(authUser, null, emptyList())

    @Nested
    @DisplayName("GET /api/users/me")
    inner class GetMeTests {
        @Test
        @DisplayName("Debe retornar 200 OK con los datos del usuario autenticado")
        fun `should return user profile`() {
            `when`(getUserUseCase.getById(userId)).thenReturn(mockUser)

            mockMvc.perform(
                get("/api/users/me")
                    .with(authentication(authToken)),
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.email").value("david@test.com"))
                .andExpect(jsonPath("$.username").value("david_85"))
                .andExpect(jsonPath("$.avatarNum").value(1))
        }
    }

    @Nested
    @DisplayName("PATCH /api/users/me")
    inner class UpdateProfileTests {
        @Test
        @DisplayName("Debe retornar 200 OK al actualizar el perfil")
        fun `should update user profile`() {
            val request = UpdateUserProfileRequest(description = "Nueva", avatarNum = 2)
            val updatedUser = mockUser.copy(description = "Nueva", avatarNum = 2)

            `when`(updateUserUseCase.updateProfile(userId, "Nueva", 2)).thenReturn(updatedUser)

            mockMvc.perform(
                patch("/api/users/me")
                    .with(authentication(authToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.description").value("Nueva"))
                .andExpect(jsonPath("$.avatarNum").value(2))
        }
    }

    @Nested
    @DisplayName("PATCH /api/users/me/password")
    inner class ChangePasswordTests {
        @Test
        @DisplayName("Debe retornar 204 No Content al cambiar la contraseña con éxito")
        fun `should change password successfully`() {
            val requestBody = """{"currentPassword": "old", "newPassword": "new12345"}"""

            mockMvc.perform(
                patch("/api/users/me/password")
                    .with(authentication(authToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody),
            )
                .andExpect(status().isNoContent)
        }

        @Test
        @DisplayName("Debe retornar 400 si la nueva contraseña es muy corta")
        fun `should return 400 when new password is too short`() {
            val requestBody = """{"currentPassword": "old", "newPassword": "123"}"""

            mockMvc.perform(
                patch("/api/users/me/password")
                    .with(authentication(authToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody),
            )
                .andExpect(status().isBadRequest)
        }
    }
}
