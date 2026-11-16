package com.bestbidder.adapter.web.auction

import com.bestbidder.adapter.web.auction.dto.AuctionResponse
import com.bestbidder.adapter.web.auction.dto.CreateAuctionRequest
import com.bestbidder.adapter.web.auction.dto.SearchAuctionRequest
import com.bestbidder.usecase.auction.CreateAuctionUseCase
import com.bestbidder.usecase.auction.GetAuctionUseCase
import com.bestbidder.usecase.port.CardRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Controlador REST encargado de la gestión y exposición de endpoints relacionales de subastas.
 */
@RestController
@RequestMapping("/api/auction")
class AuctionController(
    private val createAuctionUseCase: CreateAuctionUseCase,
    private val getAuctionUseCase: GetAuctionUseCase,
    private val cardRepository: CardRepository,
) {
    /**
     * Registra una nueva subasta asociada a una carta previamente guardada.
     */
    @PostMapping("/create")
    fun create(
        @RequestBody request: CreateAuctionRequest,
    ): ResponseEntity<AuctionResponse> {
        val card =
            cardRepository.getById(request.card_id)
                ?: throw IllegalArgumentException("No existe la carta con ID ${request.card_id}")

        val auction =
            createAuctionUseCase.create(
                creatorId = request.creator_id,
                creatorUsername = request.creator_username,
                card = card,
                description = request.description,
                minPrize = request.min_prize,
                startedAt = request.started_at,
                durationInDays = request.duration_in_days,
            )

        return ResponseEntity.status(HttpStatus.CREATED).body(AuctionResponse.fromDomain(auction))
    }

    /**
     * Recupera el catálogo completo de subastas del sistema.
     */
    @GetMapping("/all")
    fun getAll(): ResponseEntity<List<AuctionResponse>> {
        val auctions = getAuctionUseCase.getAll().map { AuctionResponse.fromDomain(it) }
        return ResponseEntity.ok(auctions)
    }

    /**
     * Busca y filtra subastas combinando múltiples criterios opcionales.
     */
    @GetMapping("/search")
    fun search(request: SearchAuctionRequest): ResponseEntity<List<AuctionResponse>> {
        val auctions = getAuctionUseCase.search(request.toCriteria()).map { AuctionResponse.fromDomain(it) }
        return ResponseEntity.ok(auctions)
    }
}
