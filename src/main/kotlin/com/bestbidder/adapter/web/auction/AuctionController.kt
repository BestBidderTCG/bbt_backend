package com.bestbidder.adapter.web.auction

import com.bestbidder.adapter.web.auction.dto.AuctionDetailResponse
import com.bestbidder.adapter.web.auction.dto.AuctionListItemResponse
import com.bestbidder.adapter.web.auction.dto.AuctionResponse
import com.bestbidder.adapter.web.auction.dto.AuctionSummaryResponse
import com.bestbidder.adapter.web.auction.dto.CreateAuctionRequest
import com.bestbidder.adapter.web.auction.dto.EditAuctionRequest
import com.bestbidder.adapter.web.auction.dto.FavoriteAuctionResponse
import com.bestbidder.domain.auction.NotificationMode
import com.bestbidder.infrastructure.security.AuthenticatedUser
import com.bestbidder.usecase.auction.AuctionSort
import com.bestbidder.usecase.auction.CreateAuctionUseCase
import com.bestbidder.usecase.auction.EditAuctionCommand
import com.bestbidder.usecase.auction.EditAuctionUseCase
import com.bestbidder.usecase.auction.GetAuctionUseCase
import com.bestbidder.usecase.auction.ManageFavoriteAuctionUseCase
import com.bestbidder.usecase.auction.RegisterAuctionViewUseCase
import com.bestbidder.usecase.port.CardRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
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
    private val registerAuctionViewUseCase: RegisterAuctionViewUseCase,
    private val editAuctionUseCase: EditAuctionUseCase,
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
     * Edita una subasta que aún no ha empezado. Solo el creador puede hacerlo.
     */
    @PatchMapping("/{id}")
    fun edit(
        @PathVariable id: UUID,
        @AuthenticationPrincipal principal: AuthenticatedUser,
        @RequestBody request: EditAuctionRequest,
    ): ResponseEntity<AuctionResponse> {
        val auction =
            editAuctionUseCase.edit(
                EditAuctionCommand(
                    auctionId = id,
                    requesterUserId = principal.id,
                    description = request.description,
                    minPrize = request.minPrize,
                    startedAt = request.startedAt,
                    durationInDays = request.durationInDays,
                ),
            )
        return ResponseEntity.ok(AuctionResponse.fromDomain(auction))
    }

    /**
     * Recupera el detalle de una subasta. Si hay usuario autenticado, indica si es favorita.
     */
    @GetMapping("/{id}")
    fun getById(
        @PathVariable id: UUID,
        @AuthenticationPrincipal principal: AuthenticatedUser,
    ): ResponseEntity<AuctionDetailResponse> {
        val detail = getAuctionUseCase.getDetail(id, principal.id) ?: return ResponseEntity.notFound().build()

        // creator == null significa que quien consulta es el propietario: sus visitas no cuentan
        if (detail.creator != null) {
            registerAuctionViewUseCase.register(id, principal.id)
        }
        return ResponseEntity.ok(AuctionDetailResponse.fromDomain(detail))
    }

    /**
     * Recupera el listado de subastas en curso, ordenado según el criterio indicado.
     */
    @GetMapping("/all")
    fun getAll(
        @RequestParam(defaultValue = "POPULARITY") sort: AuctionSort,
    ): ResponseEntity<List<AuctionListItemResponse>> {
        val auctions = getAuctionUseCase.getAll(sort).map { AuctionListItemResponse.fromDomain(it) }
        return ResponseEntity.ok(auctions)
    }

    /**
     * Recupera el listado de mis subastas.
     */
    @GetMapping("/user/my-auctions")
    fun getMyAuctions(
        @AuthenticationPrincipal principal: AuthenticatedUser,
    ): ResponseEntity<List<AuctionSummaryResponse>> {
        val auctions = getAuctionUseCase.getUserAuctions(principal.id).map { AuctionSummaryResponse.fromDomain(it) }
        return ResponseEntity.ok(auctions)
    }

    /**
     * Añade una subasta a la lista de favoritas del usuario autenticado con el modo de notificación indicado.
     * Si ya era favorita, actualiza su modo de notificación.
     */
    @PostMapping("/{auctionId}/favorite/{notificationMode}")
    fun addFavorite(
        @PathVariable auctionId: UUID,
        @PathVariable notificationMode: NotificationMode,
        @AuthenticationPrincipal principal: AuthenticatedUser,
    ): ResponseEntity<Void> {
        manageFavoriteAuctionUseCase.addFavorite(
            userId = principal.id,
            auctionId = auctionId,
            notificationMode = notificationMode,
        )
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    /**
     * Cambia el modo de notificación de una subasta que ya está en favoritos del usuario autenticado.
     */
    @PatchMapping("/{auctionId}/favorite/{notificationMode}")
    fun changeNotificationMode(
        @PathVariable auctionId: UUID,
        @PathVariable notificationMode: NotificationMode,
        @AuthenticationPrincipal principal: AuthenticatedUser,
    ): ResponseEntity<Void> {
        manageFavoriteAuctionUseCase.changeNotificationMode(
            userId = principal.id,
            auctionId = auctionId,
            notificationMode = notificationMode,
        )
        return ResponseEntity.noContent().build()
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
     * Obtiene todas las subastas favoritas del usuario autenticado, con su modo de notificación.
     */
    @GetMapping("/favorites")
    fun getFavorites(
        @AuthenticationPrincipal principal: AuthenticatedUser,
    ): ResponseEntity<List<FavoriteAuctionResponse>> {
        val favorites =
            manageFavoriteAuctionUseCase
                .getUserFavorites(principal.id)
                .map { FavoriteAuctionResponse.fromDomain(it) }
        return ResponseEntity.ok(favorites)
    }
}
