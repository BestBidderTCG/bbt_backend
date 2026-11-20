package com.bestbidder.adapter.web.bid

import com.bestbidder.adapter.web.bid.dto.AuctionHistoryResponse
import com.bestbidder.adapter.web.bid.dto.BidderHistoryResponse
import com.bestbidder.adapter.web.bid.dto.PlaceBidRequest
import com.bestbidder.domain.bid.Bid
import com.bestbidder.infrastructure.security.AuthenticatedUser
import com.bestbidder.usecase.bid.GetBidsUseCase
import com.bestbidder.usecase.bid.PlaceBidUseCase
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

/**
 * Controlador REST para el registro y consulta del historial de pujas sobre las subastas.
 */
@RestController
@RequestMapping("/api/bid")
class BidController(
    private val placeBidUseCase: PlaceBidUseCase,
    private val getBidsUseCase: GetBidsUseCase,
) {
    /**
     * Procesa y registra una puja en una subasta especificada en la ruta.
     */
    @PostMapping("/{auctionId}")
    fun placeBid(
        @PathVariable auctionId: UUID,
        @AuthenticationPrincipal principal: AuthenticatedUser,
        @RequestBody request: PlaceBidRequest,
    ): ResponseEntity<Bid> {
        val bid =
            placeBidUseCase.execute(
                auctionId = auctionId,
                bidderId = principal.id,
                bidderUsername = principal.username,
                amount = request.amount,
            )

        return ResponseEntity.status(HttpStatus.CREATED).body(bid)
    }

    /**
     * Obtiene el historial completo de pujas realizadas sobre una subasta determinada.
     */
    @GetMapping("/history/{auctionId}")
    fun getAuctionHistory(
        @PathVariable auctionId: UUID,
    ): ResponseEntity<List<AuctionHistoryResponse>> {
        val history =
            getBidsUseCase
                .byAuction(auctionId)
                .map { AuctionHistoryResponse.fromDomain(it) }
        return ResponseEntity.ok(history)
    }

    /**
     * Recupera el historial de pujas del usuario autenticado.
     */
    @GetMapping("/user/my-bids")
    fun getMyBids(
        @AuthenticationPrincipal principal: AuthenticatedUser,
    ): ResponseEntity<List<BidderHistoryResponse>> {
        val bids =
            getBidsUseCase
                .byBidder(principal.id)
                .map { BidderHistoryResponse.fromDomain(it) }
        return ResponseEntity.ok(bids)
    }
}
