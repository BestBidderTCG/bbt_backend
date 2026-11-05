package com.bestbidder.adapter.web.bid

import com.bestbidder.adapter.web.bid.dto.PlaceBidRequest
import com.bestbidder.adapter.web.exception.GlobalExceptionHandler
import com.bestbidder.domain.bid.Bid
import com.bestbidder.infrastructure.security.AuthenticatedUser
import com.bestbidder.usecase.bid.GetBidsByAuctionUseCase
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
    private lateinit var getBidsByAuctionUseCase: GetBidsByAuctionUseCase

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
        @DisplayName("Debe retornar 200 OK con el historial de pujas")
        fun `should return 200 OK with bid history`() {
            val bid1 = Bid(auctionId = auctionId, bidderId = userId, bidderUsername = "pujador1", amount = 200.0)
            val bid2 = Bid(auctionId = auctionId, bidderId = UUID.randomUUID(), bidderUsername = "pujador2", amount = 150.0)

            `when`(getBidsByAuctionUseCase.execute(auctionId)).thenReturn(listOf(bid1, bid2))

            mockMvc.perform(get("/api/bid/history/$auctionId"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].amount").value(200.0))
                .andExpect(jsonPath("$[0].bidderUsername").value("pujador1"))
                .andExpect(jsonPath("$[1].amount").value(150.0))

            verify(getBidsByAuctionUseCase, times(1)).execute(auctionId)
        }
    }
}
