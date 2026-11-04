package com.bestbidder.adapter.web.bid

import com.bestbidder.adapter.web.bid.dto.PlaceBidRequest
import com.bestbidder.domain.bid.Bid
import com.bestbidder.usecase.bid.GetBidsByAuctionUseCase
import com.bestbidder.usecase.bid.PlaceBidUseCase
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/bid")
class BidController(
    private val placeBidUseCase: PlaceBidUseCase,
    private val getBidsByAuctionUseCase: GetBidsByAuctionUseCase,
) {
    /**
     * POST /api/bid/{auctionId}
     * Realiza una nueva puja en la subasta especificada.
     */
    @PostMapping("/{auctionId}")
    fun placeBid(
        @PathVariable auctionId: UUID,
        @RequestBody request: PlaceBidRequest,
    ): ResponseEntity<Bid> {
        val bid =
            placeBidUseCase.execute(
                auctionId = auctionId,
                bidderId = request.bidder_id,
                bidderUsername = request.bidder_username,
                amount = request.amount,
            )

        return ResponseEntity.status(HttpStatus.CREATED).body(bid)
    }

    /**
     * GET /api/bid/history/{auctionId}
     * Obtiene el historial completo de pujas de una subasta en orden descendente.
     */
    @GetMapping("/history/{auctionId}")
    fun getAuctionHistory(
        @PathVariable auctionId: UUID,
    ): ResponseEntity<List<Bid>> {
        val history = getBidsByAuctionUseCase.execute(auctionId)
        return ResponseEntity.ok(history)
    }
}
