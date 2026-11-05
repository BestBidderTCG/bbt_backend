package com.bestbidder.adapter.web.bid

import com.bestbidder.adapter.web.bid.dto.PlaceBidRequest
import com.bestbidder.domain.bid.Bid
import com.bestbidder.infrastructure.security.AuthenticatedUser
import com.bestbidder.usecase.bid.GetBidsByAuctionUseCase
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

@RestController
@RequestMapping("/api/bid")
class BidController(
    private val placeBidUseCase: PlaceBidUseCase,
    private val getBidsByAuctionUseCase: GetBidsByAuctionUseCase,
) {
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

    @GetMapping("/history/{auctionId}")
    fun getAuctionHistory(
        @PathVariable auctionId: UUID,
    ): ResponseEntity<List<Bid>> {
        val history = getBidsByAuctionUseCase.execute(auctionId)
        return ResponseEntity.ok(history)
    }
}
