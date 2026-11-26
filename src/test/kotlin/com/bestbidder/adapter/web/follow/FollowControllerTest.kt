package com.bestbidder.adapter.web.follow

import com.bestbidder.adapter.web.exception.GlobalExceptionHandler
import com.bestbidder.domain.auction.AuctionListItem
import com.bestbidder.domain.auction.AuctionStatus
import com.bestbidder.domain.card.CardState
import com.bestbidder.domain.follow.FollowListItem
import com.bestbidder.domain.follow.FollowStats
import com.bestbidder.domain.user.exceptions.UserNotFoundException
import com.bestbidder.infrastructure.security.AuthenticatedUser
import com.bestbidder.usecase.follow.GetFollowUseCase
import com.bestbidder.usecase.follow.ManageFollowUseCase
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

@WebMvcTest(FollowController::class)
@Import(GlobalExceptionHandler::class)
class FollowControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var manageFollowUseCase: ManageFollowUseCase

    @MockBean
    private lateinit var getFollowUseCase: GetFollowUseCase

    private val userId = UUID.randomUUID()
    private val otherId = UUID.randomUUID()

    private fun setAuthenticatedUser(
        userId: UUID,
        username: String,
    ) {
        val principal = AuthenticatedUser(id = userId, username = username)
        val auth = UsernamePasswordAuthenticationToken(principal, null, emptyList())
        SecurityContextHolder.getContext().authentication = auth
    }

    private val followed = FollowListItem(id = otherId, username = "amigo", avatarNum = 2, followedByMe = true)

    @Nested
    @DisplayName("POST /api/social/{id}/follow")
    inner class FollowTests {
        @Test
        @DisplayName("Debe retornar 201 Created al seguir a un usuario")
        fun `should return 201 on follow`() {
            setAuthenticatedUser(userId, "paula")
            `when`(manageFollowUseCase.follow(userId, otherId)).thenReturn(true)

            mockMvc.perform(post("/api/social/$otherId/follow"))
                .andExpect(status().isCreated)

            verify(manageFollowUseCase, times(1)).follow(userId, otherId)
        }

        @Test
        @DisplayName("Debe retornar 201 Created aunque ya lo siguiera (idempotente)")
        fun `should return 201 when already following`() {
            setAuthenticatedUser(userId, "paula")
            `when`(manageFollowUseCase.follow(userId, otherId)).thenReturn(false)

            mockMvc.perform(post("/api/social/$otherId/follow"))
                .andExpect(status().isCreated)
        }

        @Test
        @DisplayName("Debe retornar 400 Bad Request al intentar seguirse a sí mismo")
        fun `should return 400 when following yourself`() {
            setAuthenticatedUser(userId, "paula")
            `when`(manageFollowUseCase.follow(userId, userId))
                .thenThrow(IllegalArgumentException("No puedes seguirte a ti mismo"))

            mockMvc.perform(post("/api/social/$userId/follow"))
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("No puedes seguirte a ti mismo"))
        }

        @Test
        @DisplayName("Debe retornar 404 Not Found cuando el usuario no existe")
        fun `should return 404 when user does not exist`() {
            setAuthenticatedUser(userId, "paula")
            `when`(manageFollowUseCase.follow(userId, otherId)).thenThrow(UserNotFoundException(otherId))

            mockMvc.perform(post("/api/social/$otherId/follow"))
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.status").value(404))
        }
    }

    @Nested
    @DisplayName("DELETE /api/social/{id}/follow")
    inner class UnfollowTests {
        @Test
        @DisplayName("Debe retornar 204 No Content al dejar de seguir")
        fun `should return 204 on unfollow`() {
            setAuthenticatedUser(userId, "paula")

            mockMvc.perform(delete("/api/social/$otherId/follow"))
                .andExpect(status().isNoContent)

            verify(manageFollowUseCase, times(1)).unfollow(userId, otherId)
        }
    }

    @Nested
    @DisplayName("GET /api/social/me/following y /me/followers")
    inner class MyListsTests {
        @Test
        @DisplayName("Debe retornar 200 OK con mis seguidos usando página 0 y tamaño 20 por defecto")
        fun `should return my following with defaults`() {
            setAuthenticatedUser(userId, "paula")
            `when`(getFollowUseCase.getFollowing(userId, userId, 0, 20)).thenReturn(listOf(followed))

            mockMvc.perform(get("/api/social/me/following"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(otherId.toString()))
                .andExpect(jsonPath("$[0].username").value("amigo"))
                .andExpect(jsonPath("$[0].avatarNum").value(2))
                .andExpect(jsonPath("$[0].followedByMe").value(true))

            verify(getFollowUseCase, times(1)).getFollowing(userId, userId, 0, 20)
        }

        @Test
        @DisplayName("Debe pasar page y size al caso de uso")
        fun `should pass page and size`() {
            setAuthenticatedUser(userId, "paula")
            `when`(getFollowUseCase.getFollowing(userId, userId, 2, 5)).thenReturn(emptyList())

            mockMvc.perform(get("/api/social/me/following?page=2&size=5"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.length()").value(0))

            verify(getFollowUseCase, times(1)).getFollowing(userId, userId, 2, 5)
        }

        @Test
        @DisplayName("Debe retornar 200 OK con mis seguidores")
        fun `should return my followers`() {
            setAuthenticatedUser(userId, "paula")
            `when`(getFollowUseCase.getFollowers(userId, userId, 0, 20)).thenReturn(listOf(followed))

            mockMvc.perform(get("/api/social/me/followers"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$[0].username").value("amigo"))

            verify(getFollowUseCase, times(1)).getFollowers(userId, userId, 0, 20)
        }
    }

    @Nested
    @DisplayName("GET /api/social/me/following/auctions")
    inner class FollowingAuctionsTests {
        private val item =
            AuctionListItem(
                id = UUID.randomUUID(),
                cardName = "Charizard",
                cardImage = "charizard.png",
                currentBid = 120.5,
                remainingDays = 3,
                cardState = CardState.NEW,
                cardCategory = "Pokémon",
                cardCollection = "Base Set",
                cardNumber = 4,
                status = AuctionStatus.IN_PROGRESS,
            )

        @Test
        @DisplayName("Debe retornar 200 OK con las subastas de mis seguidos")
        fun `should return following auctions`() {
            setAuthenticatedUser(userId, "paula")
            `when`(getFollowUseCase.getFollowingAuctions(userId, null)).thenReturn(listOf(item))

            mockMvc.perform(get("/api/social/me/following/auctions"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(item.id.toString()))
                .andExpect(jsonPath("$[0].cardName").value("Charizard"))
                .andExpect(jsonPath("$[0].currentBid").value(120.5))
                .andExpect(jsonPath("$[0].status").value("IN_PROGRESS"))

            verify(getFollowUseCase, times(1)).getFollowingAuctions(userId, null)
        }

        @Test
        @DisplayName("Debe pasar el filtro status al caso de uso")
        fun `should pass status filter`() {
            setAuthenticatedUser(userId, "paula")
            `when`(getFollowUseCase.getFollowingAuctions(userId, AuctionStatus.UPCOMING)).thenReturn(emptyList())

            mockMvc.perform(get("/api/social/me/following/auctions?status=UPCOMING"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.length()").value(0))

            verify(getFollowUseCase, times(1)).getFollowingAuctions(userId, AuctionStatus.UPCOMING)
        }

        @Test
        @DisplayName("Debe retornar 400 Bad Request cuando el estado no está permitido")
        fun `should return 400 when status is not allowed`() {
            setAuthenticatedUser(userId, "paula")
            `when`(getFollowUseCase.getFollowingAuctions(userId, AuctionStatus.FINISHED))
                .thenThrow(IllegalArgumentException("El estado solo puede ser UPCOMING o IN_PROGRESS"))

            mockMvc.perform(get("/api/social/me/following/auctions?status=FINISHED"))
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.message").value("El estado solo puede ser UPCOMING o IN_PROGRESS"))
        }

        @Test
        @DisplayName("Debe retornar 400 Bad Request cuando el estado no existe")
        fun `should return 400 when status is unknown`() {
            setAuthenticatedUser(userId, "paula")

            mockMvc.perform(get("/api/social/me/following/auctions?status=INVENTADO"))
                .andExpect(status().isBadRequest)

            verify(getFollowUseCase, never()).getFollowingAuctions(userId, null)
        }
    }

    @Nested
    @DisplayName("Perfil de otro usuario")
    inner class OtherUserTests {
        @Test
        @DisplayName("GET /{id}/following debe consultar los seguidos de ese usuario")
        fun `should return following of another user`() {
            setAuthenticatedUser(userId, "paula")
            `when`(getFollowUseCase.getFollowing(otherId, userId, 0, 20)).thenReturn(listOf(followed))

            mockMvc.perform(get("/api/social/$otherId/following"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$[0].username").value("amigo"))

            verify(getFollowUseCase, times(1)).getFollowing(otherId, userId, 0, 20)
        }

        @Test
        @DisplayName("GET /{id}/followers debe consultar los seguidores de ese usuario")
        fun `should return followers of another user`() {
            setAuthenticatedUser(userId, "paula")
            `when`(getFollowUseCase.getFollowers(otherId, userId, 0, 20)).thenReturn(listOf(followed))

            mockMvc.perform(get("/api/social/$otherId/followers"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.length()").value(1))

            verify(getFollowUseCase, times(1)).getFollowers(otherId, userId, 0, 20)
        }

        @Test
        @DisplayName("GET /{id}/following debe retornar 404 cuando el usuario no existe")
        fun `should return 404 when user does not exist`() {
            setAuthenticatedUser(userId, "paula")
            `when`(getFollowUseCase.getFollowing(otherId, userId, 0, 20)).thenThrow(UserNotFoundException(otherId))

            mockMvc.perform(get("/api/social/$otherId/following"))
                .andExpect(status().isNotFound)
        }

        @Test
        @DisplayName("GET /{id}/follow-stats debe retornar contadores y si lo sigo")
        fun `should return follow stats`() {
            setAuthenticatedUser(userId, "paula")
            `when`(getFollowUseCase.getStats(otherId, userId))
                .thenReturn(FollowStats(followersCount = 12, followingCount = 3, following = true))

            mockMvc.perform(get("/api/social/$otherId/follow-stats"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.followersCount").value(12))
                .andExpect(jsonPath("$.followingCount").value(3))
                .andExpect(jsonPath("$.following").value(true))

            verify(getFollowUseCase, times(1)).getStats(otherId, userId)
        }
    }

    @Nested
    @DisplayName("GET /api/social/me/follow-stats")
    inner class MyStatsTests {
        @Test
        @DisplayName("Debe retornar 200 OK con mis contadores sociales")
        fun `should return my follow stats`() {
            setAuthenticatedUser(userId, "paula")
            `when`(getFollowUseCase.getStats(userId, userId))
                .thenReturn(FollowStats(followersCount = 20, followingCount = 7, following = false))

            mockMvc.perform(get("/api/social/me/follow-stats"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.followersCount").value(20))
                .andExpect(jsonPath("$.followingCount").value(7))
                .andExpect(jsonPath("$.following").value(false))

            verify(getFollowUseCase, times(1)).getStats(userId, userId)
        }

        @Test
        @DisplayName("Debe retornar 404 Not Found cuando el usuario autenticado no existe")
        fun `should return 404 when authenticated user does not exist`() {
            setAuthenticatedUser(userId, "paula")
            `when`(getFollowUseCase.getStats(userId, userId)).thenThrow(UserNotFoundException(userId))

            mockMvc.perform(get("/api/social/me/follow-stats"))
                .andExpect(status().isNotFound)
        }
    }
}
