package com.bestbidder.adapter.web.bid

import com.bestbidder.adapter.web.bid.dto.PlaceBidRequest
import com.bestbidder.adapter.web.exception.GlobalExceptionHandler
import com.bestbidder.domain.auction.AuctionStatus
import com.bestbidder.domain.bid.AuctionHistory
import com.bestbidder.domain.bid.Bid
import com.bestbidder.domain.bid.BidderHistory
import com.bestbidder.infrastructure.security.AuthenticatedUser
import com.bestbidder.usecase.bid.GetBidsUseCase
import com.bestbidder.usecase.bid.PlaceBidUseCase
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

@WebMvcTest(BidController::class)
@Import(GlobalExceptionHandler::class)
class BidControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var placeBidUseCase: PlaceBidUseCase

    @MockBean
    private lateinit var getBidsUseCase: GetBidsUseCase

    private val auctionId = UUID.randomUUID()
    private val userId = UUID.randomUUID()

    private fun setAuthenticatedUser(
        userId: UUID,
        username: String,
    ) {
        val principal = AuthenticatedUser(id = userId, username = username)
        val auth = UsernamePasswordAuthenticationToken(principal, null, emptyList())
        SecurityContextHolder.getContext().authentication = auth
    }

    @Nested
    @DisplayName("POST /api/bid/{auctionId}")
    inner class PlaceBidTests {
        @Test
        @DisplayName("Debe retornar 201 Created cuando se realiza una puja válida")
        fun `should return 201 Created on valid bid`() {
            setAuthenticatedUser(userId, "pujador1")
            val request = PlaceBidRequest(amount = 150.0)

            val expectedBid =
                Bid(
                    auctionId = auctionId,
                    bidderId = userId,
                    bidderUsername = "pujador1",
                    amount = 150.0,
                )

            `when`(
                placeBidUseCase.execute(
                    auctionId = auctionId,
                    bidderId = userId,
                    bidderUsername = "pujador1",
                    amount = 150.0,
                ),
            ).thenReturn(expectedBid)

            mockMvc.perform(
                post("/api/bid/$auctionId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.auctionId").value(auctionId.toString()))
                .andExpect(jsonPath("$.bidderId").value(userId.toString()))
                .andExpect(jsonPath("$.bidderUsername").value("pujador1"))
                .andExpect(jsonPath("$.amount").value(150.0))

            verify(placeBidUseCase, times(1)).execute(
                auctionId = auctionId,
                bidderId = userId,
                bidderUsername = "pujador1",
                amount = 150.0,
            )
        }

        @Test
        @DisplayName("Debe retornar 400 Bad Request si la puja no alcanza el mínimo")
        fun `should return 400 Bad Request when bid amount is invalid`() {
            setAuthenticatedUser(userId, "pujador1")
            val request = PlaceBidRequest(amount = 10.0)

            `when`(
                placeBidUseCase.execute(
                    auctionId = any(),
                    bidderId = any(),
                    bidderUsername = any(),
                    amount = any(),
                ),
            ).thenThrow(IllegalArgumentException("El importe mínimo para pujar debe ser de 155.0 €"))

            mockMvc.perform(
                post("/api/bid/$auctionId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("El importe mínimo para pujar debe ser de 155.0 €"))
        }
    }

    @Nested
    @DisplayName("GET /api/bid/history/{auctionId}")
    inner class GetHistoryTests {
        @Test
        @DisplayName("Debe retornar 200 OK con el historial de pujas de la subasta")
        fun `should return 200 OK with auction bid history`() {
            val bidder2Id = UUID.randomUUID()
            val h1 =
                AuctionHistory(
                    bidderId = userId,
                    bidderName = "pujador1",
                    bidderAvatar = 1,
                    amount = 200.0,
                )
            val h2 =
                AuctionHistory(
                    bidderId = bidder2Id,
                    bidderName = "pujador2",
                    bidderAvatar = 2,
                    amount = 150.0,
                )

            `when`(getBidsUseCase.byAuction(auctionId)).thenReturn(listOf(h1, h2))

            mockMvc.perform(get("/api/bid/history/$auctionId"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].bidderId").value(userId.toString()))
                .andExpect(jsonPath("$[0].bidderName").value("pujador1"))
                .andExpect(jsonPath("$[0].bidderAvatar").value("a1.png"))
                .andExpect(jsonPath("$[0].amount").value(200.0))
                .andExpect(jsonPath("$[1].bidderName").value("pujador2"))
                .andExpect(jsonPath("$[1].amount").value(150.0))

            verify(getBidsUseCase, times(1)).byAuction(auctionId)
        }
    }

    @Nested
    @DisplayName("GET /api/bid/user/my-bids")
    inner class GetMyBidsTests {
        @Test
        @DisplayName("Debe retornar 200 OK con el historial de pujas del usuario autenticado")
        fun `should return 200 OK with my bids`() {
            setAuthenticatedUser(userId, "pujador1")
            val h1 =
                BidderHistory(
                    auctionId = auctionId,
                    cardName = "Charizard",
                    cardImage = "charizard.png",
                    amount = 300.0,
                    status = AuctionStatus.IN_PROGRESS,
                    iAmWinning = true,
                )

            `when`(getBidsUseCase.byBidder(userId)).thenReturn(listOf(h1))

            mockMvc.perform(get("/api/bid/user/my-bids"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].auctionId").value(auctionId.toString()))
                .andExpect(jsonPath("$[0].cardName").value("Charizard"))
                .andExpect(jsonPath("$[0].amount").value(300.0))
                .andExpect(jsonPath("$[0].status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$[0].iAmWinning").value(true))

            verify(getBidsUseCase, times(1)).byBidder(userId)
        }
    }
}
