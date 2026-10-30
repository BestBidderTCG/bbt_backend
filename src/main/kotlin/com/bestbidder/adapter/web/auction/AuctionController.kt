package com.bestbidder.adapter.web.auction

import com.bestbidder.adapter.web.auction.dto.AuctionResponse
import com.bestbidder.adapter.web.auction.dto.CreateAuctionRequest
import com.bestbidder.domain.auction.Auction
import com.bestbidder.infrastructure.notification.AuctionNotificationAdapter
import com.bestbidder.usecase.auction.CancelAuctionUseCase
import com.bestbidder.usecase.auction.CreateAuctionUseCase
import com.bestbidder.usecase.auction.GetAuctionUseCase
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.UUID

@RestController
@RequestMapping("/api/auction")
class AuctionController(
    private val createAuctionUseCase: CreateAuctionUseCase,
    private val getAuctionUseCase: GetAuctionUseCase,
    private val cancelAuctionUseCase: CancelAuctionUseCase,
    private val sseNotificationAdapter: AuctionNotificationAdapter,
) {
    /**
     * Endpoint de Creación de Subasta
     * Requiere un cuerpo JSON con los datos de la subasta a crear
     */
    @PostMapping("/create")
    fun create(
        @RequestBody request: CreateAuctionRequest,
    ): ResponseEntity<AuctionResponse> {
        val auction =
            createAuctionUseCase.create(
                // Hay que cambiarlo para que sea un getIdByUsername() pero no existe
                creator_id = request.creator_id,
                creator_username = request.creator_username,
                card = request.card,
                collection = request.collection,
                minPrize = request.min_prize,
                startedAt = request.started_at,
                durationInDays = request.duration_in_days,
            )

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(AuctionResponse.fromDomain(auction))
    }

    /**
     * Endpoint para obtener todas las subastas
     * - probablemente desaparecerá en el futuro porque engloba canceladas/expiradas (útil para pruebas)
     */
    @GetMapping("/all")
    fun getAll(): ResponseEntity<List<AuctionResponse>> {
        val auctions = getAuctionUseCase.getAll()
        val response = auctions.map { auction: Auction -> AuctionResponse.fromDomain(auction) }

        return ResponseEntity.ok(response)
    }

    /**
     * Endpoint para obtener todas las subastas activas
     */
    @GetMapping("/active")
    fun getActive(): ResponseEntity<List<AuctionResponse>> {
        val auctions = getAuctionUseCase.getActive()
        val response = auctions.map { auction: Auction -> AuctionResponse.fromDomain(auction) }

        return ResponseEntity.ok(response)
    }

    /**
     * Endpoint de Búsquedas por Filtros (Carta, Colección, Creador)
     */
    @GetMapping("/search")
    fun search(
        @RequestParam(required = false) creator: String?,
        @RequestParam(required = false) collection: String?,
        @RequestParam(required = false) card: String?,
    ): ResponseEntity<List<AuctionResponse>> {
        val auctions =
            when {
                creator != null -> getAuctionUseCase.getByCreator(creator)
                collection != null -> getAuctionUseCase.getByCollection(collection)
                card != null -> getAuctionUseCase.getByCard(card)
                else -> getAuctionUseCase.getAll()
            }

        val response = auctions.map { AuctionResponse.fromDomain(it) }
        return ResponseEntity.ok(response)
    }

    /**
     * Endpoint para Cancelar una Subasta
     * En el fututo se puede emplear el token guardado
     */
    @PutMapping("/cancel/{id}")
    fun cancel(
        @PathVariable id: UUID,
        @RequestParam requesterCreator: UUID,
    ): ResponseEntity<Unit> {
        cancelAuctionUseCase.cancel(
            auctionId = id,
            requesterUserId = requesterCreator,
        )
        return ResponseEntity.noContent().build()
    }

    /**
     * Endpoint SSE para que la App Móvil se suscriba a eventos en tiempo real
     */
    @GetMapping("/subscribe")
    fun subscribe(): SseEmitter {
        // 0L o Long.MAX_VALUE permite conexiones de larga duración
        val emitter = SseEmitter(Long.MAX_VALUE)

        sseNotificationAdapter.addEmitter(emitter)

        // evento inicial de bienvenida/ping
        runCatching {
            emitter.send(
                SseEmitter.event()
                    .name("INIT")
                    .data("Connected to auction events"),
            )
        }.onFailure {
            emitter.completeWithError(it)
        }

        return emitter
    }
}
