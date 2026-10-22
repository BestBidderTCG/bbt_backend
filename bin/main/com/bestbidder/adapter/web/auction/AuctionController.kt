package com.bestbidder.adapter.web.auction

import com.bestbidder.adapter.web.auction.dto.AuctionResponse
import com.bestbidder.adapter.web.auction.dto.CreateAuctionRequest
import com.bestbidder.usecase.auction.CreateAuctionUseCase
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auction")
class AuctionController(
    private val createAuctionUseCase: CreateAuctionUseCase,
) {
    @PostMapping("/create")
    fun create(
        @RequestBody request: CreateAuctionRequest,
    ): ResponseEntity<AuctionResponse> {
        val auction =
            createAuctionUseCase.create(
                creator = request.creator,
                card = request.card,
                collection = request.collection,
                minPrize = request.min_prize,
            )

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(AuctionResponse.fromDomain(auction))
    }
}
