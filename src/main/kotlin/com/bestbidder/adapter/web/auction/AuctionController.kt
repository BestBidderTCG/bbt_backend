package com.bestbidder.adapter.web.auction

import com.bestbidder.adapter.web.auction.dto.AuctionResponse
import com.bestbidder.adapter.web.auction.dto.CreateAuctionRequest
import com.bestbidder.adapter.web.auction.dto.SearchAuctionRequest
import com.bestbidder.infrastructure.security.AuthenticatedUser
import com.bestbidder.usecase.auction.CreateAuctionUseCase
import com.bestbidder.usecase.auction.GetAuctionUseCase
import com.bestbidder.usecase.auction.ManageFavoriteAuctionUseCase
import com.bestbidder.usecase.port.CardRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

/**
 * Controlador REST encargado de la gestión y exposición de endpoints relacionales de subastas.
 */
@RestController
@RequestMapping("/api/auction")
class AuctionController(
    private val createAuctionUseCase: CreateAuctionUseCase,
    private val getAuctionUseCase: GetAuctionUseCase,
    private val cardRepository: CardRepository,
    private val manageFavoriteAuctionUseCase: ManageFavoriteAuctionUseCase,
) {
    /**
     * Registra una nueva subasta asociada a una carta previamente guardada.
     */
    @PostMapping("/create")
    fun create(
        @AuthenticationPrincipal principal: AuthenticatedUser,
        @RequestBody request: CreateAuctionRequest,
    ): ResponseEntity<AuctionResponse> {
        val card =
            cardRepository.getById(request.cardId)
                ?: throw IllegalArgumentException("No existe la carta con ID ${request.cardId}")

        require(card.ownerId == principal.id) {
            "No puedes crear una subasta con una carta que no te pertenece"
        }

        val auction =
            createAuctionUseCase.create(
                creatorId = principal.id,
                creatorUsername = principal.username,
                card = card,
                description = request.description,
                minPrize = request.minPrize,
                startedAt = request.startedAt,
                durationInDays = request.durationInDays,
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

    /**
     * Añade una subasta a la lista de favoritas del usuario autenticado.
     */
    @PostMapping("/{auctionId}/favorite")
    fun addFavorite(
        @PathVariable auctionId: UUID,
        @AuthenticationPrincipal principal: AuthenticatedUser,
    ): ResponseEntity<Void> {
        manageFavoriteAuctionUseCase.addFavorite(
            userId = principal.id,
            auctionId = auctionId,
        )
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    /**
     * Elimina una subasta de la lista de favoritas del usuario autenticado.
     */
    @DeleteMapping("/{auctionId}/favorite")
    fun removeFavorite(
        @PathVariable auctionId: UUID,
        @AuthenticationPrincipal principal: AuthenticatedUser,
    ): ResponseEntity<Void> {
        manageFavoriteAuctionUseCase.removeFavorite(
            userId = principal.id,
            auctionId = auctionId,
        )
        return ResponseEntity.noContent().build()
    }

    /**
     * Obtiene todas las subastas favoritas del usuario autenticado.
     */
    @GetMapping("/favorites")
    fun getFavorites(
        @AuthenticationPrincipal principal: AuthenticatedUser,
    ): ResponseEntity<List<AuctionResponse>> {
        val favorites =
            manageFavoriteAuctionUseCase
                .getUserFavorites(principal.id)
                .map { AuctionResponse.fromDomain(it) }
        return ResponseEntity.ok(favorites)
    }
}
