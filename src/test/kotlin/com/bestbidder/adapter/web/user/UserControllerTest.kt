package com.bestbidder.adapter.web.user

import com.bestbidder.adapter.web.exception.GlobalExceptionHandler
import com.bestbidder.adapter.web.user.dto.UpdateUserProfileRequest
import com.bestbidder.domain.user.User
import com.bestbidder.domain.user.exceptions.AccountDeletionBlockedException
import com.bestbidder.infrastructure.security.AuthenticatedUser
import com.bestbidder.usecase.user.AccountDeletionCheckResult
import com.bestbidder.usecase.user.CancelAccountDeletionUseCase
import com.bestbidder.usecase.user.ChangePasswordUseCase
import com.bestbidder.usecase.user.CheckAccountDeletionUseCase
import com.bestbidder.usecase.user.GetUserUseCase
import com.bestbidder.usecase.user.RequestAccountDeletionUseCase
import com.bestbidder.usecase.user.UpdateUserUseCase
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
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
    private lateinit var requestAccountDeletionUseCase: RequestAccountDeletionUseCase

    @MockBean
    private lateinit var cancelAccountDeletionUseCase: CancelAccountDeletionUseCase

    @MockBean
    private lateinit var checkAccountDeletionUseCase: CheckAccountDeletionUseCase

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
    @DisplayName("GET /api/users/{id}")
    inner class GetByIdTests {
        @Test
        @DisplayName("Debe retornar el perfil público")
        fun `should return public profile`() {
            `when`(getUserUseCase.getById(userId)).thenReturn(mockUser)

            mockMvc.perform(
                get("/api/users/$userId")
                    .with(authentication(authToken)),
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.username").value("david_85"))
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
            `when`(getUserUseCase.getById(userId)).thenReturn(updatedUser)

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

    @Nested
    @DisplayName("DELETE /api/users/me")
    inner class RequestAccountDeletionTests {
        @Test
        @DisplayName("Debe retornar 202 Accepted al solicitar la eliminación de cuenta")
        fun `should request account deletion successfully`() {
            val requestedAt = Instant.now()
            val pendingUser = mockUser.copy(deletionRequestedAt = requestedAt)

            `when`(requestAccountDeletionUseCase.execute(userId)).thenReturn(pendingUser)

            mockMvc.perform(
                delete("/api/users/me")
                    .with(authentication(authToken)),
            )
                .andExpect(status().isAccepted)
                .andExpect(jsonPath("$.status").value("PENDING_DELETION"))
                .andExpect(jsonPath("$.deletionRequestedAt").isNotEmpty)

            verify(requestAccountDeletionUseCase, times(1)).execute(userId)
        }

        @Test
        @DisplayName("Debe retornar 409 Conflict cuando la eliminación está bloqueada por subastas o pujas")
        fun `should return 409 when deletion is blocked`() {
            val blockers = listOf("tienes subastas activas o pendientes de comenzar")
            `when`(requestAccountDeletionUseCase.execute(userId))
                .thenThrow(AccountDeletionBlockedException(blockers))

            mockMvc.perform(
                delete("/api/users/me")
                    .with(authentication(authToken)),
            )
                .andExpect(status().isConflict)
                .andExpect(jsonPath("$.status").value(409))
        }
    }

    @Nested
    @DisplayName("POST /api/users/me/cancel-deletion")
    inner class CancelAccountDeletionTests {
        @Test
        @DisplayName("Debe retornar 204 No Content al cancelar la solicitud de eliminación")
        fun `should cancel account deletion successfully`() {
            mockMvc.perform(
                post("/api/users/me/cancel-deletion")
                    .with(authentication(authToken)),
            )
                .andExpect(status().isNoContent)

            verify(cancelAccountDeletionUseCase, times(1)).execute(userId)
        }
    }

    @Nested
    @DisplayName("GET /api/users/me/deletion-check")
    inner class CheckAccountDeletionTests {
        @Test
        @DisplayName("Debe retornar 200 OK con el resultado de las comprobaciones de eliminación")
        fun `should return deletion check result`() {
            `when`(checkAccountDeletionUseCase.execute(userId))
                .thenReturn(AccountDeletionCheckResult(hasActiveAuction = true, isWinningAuction = false))

            mockMvc.perform(
                get("/api/users/me/deletion-check")
                    .with(authentication(authToken)),
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.hasActiveAuction").value(true))
                .andExpect(jsonPath("$.isWinningAuction").value(false))

            verify(checkAccountDeletionUseCase, times(1)).execute(userId)
        }
    }
}
