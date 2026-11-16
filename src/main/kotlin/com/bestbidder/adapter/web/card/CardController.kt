package com.bestbidder.adapter.web.card

import com.bestbidder.adapter.web.card.dto.CardSummaryResponse
import com.bestbidder.adapter.web.card.dto.CreateCardRequest
import com.bestbidder.adapter.web.card.dto.UpdateCardRequest
import com.bestbidder.domain.card.Card
import com.bestbidder.domain.card.CardCatalog
import com.bestbidder.infrastructure.security.AuthenticatedUser
import com.bestbidder.usecase.card.CreateCardUseCase
import com.bestbidder.usecase.card.DeleteCardUseCase
import com.bestbidder.usecase.card.GetCardUseCase
import com.bestbidder.usecase.card.UpdateCardUseCase
import com.bestbidder.usecase.port.FileStoragePort
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

/**
 * Controlador REST encargado del mantenimiento y consulta del inventario de cartas coleccionables.
 */
@RestController
@RequestMapping("/api/card")
class CardController(
    private val createCardUseCase: CreateCardUseCase,
    private val getCardUseCase: GetCardUseCase,
    private val deleteCardUseCase: DeleteCardUseCase,
    private val updateCardUseCase: UpdateCardUseCase,
    private val fileStoragePort: FileStoragePort,
    private val objectMapper: ObjectMapper,
) {
    /**
     * Crea y guarda una nueva carta asignando la propiedad al usuario autenticado.
     */
    @PostMapping("/create", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun createCard(
        @AuthenticationPrincipal principal: AuthenticatedUser,
        @RequestPart("data") dataJson: String,
        @RequestPart("image") imageFile: MultipartFile,
    ): ResponseEntity<Card> {
        val request = objectMapper.readValue(dataJson, CreateCardRequest::class.java)

        require(CardCatalog.isValid(request.category, request.collection)) {
            "La categoría o colección enviada no es válida"
        }

        val imageUrl = fileStoragePort.store(imageFile)

        val card =
            createCardUseCase.execute(
                ownerId = principal.id,
                name = request.name,
                image = imageUrl,
                category = request.category,
                collection = request.collection,
                num = request.num,
                state = request.state,
                description = request.description,
            )
        return ResponseEntity.status(HttpStatus.CREATED).body(card)
    }

    /**
     * Retorna todas las cartas registradas en la plataforma.
     */
    @GetMapping("/catalog")
    fun getCatalog(): ResponseEntity<Map<String, List<String>>> {
        return ResponseEntity.ok(CardCatalog.catalog)
    }

    /**
     * Obtiene la información detallada de una carta buscando por su ID.
     */
    @GetMapping("/{id}")
    fun getCardById(
        @PathVariable id: UUID,
    ): ResponseEntity<Card> {
        val card = getCardUseCase.getById(id) ?: throw IllegalArgumentException("No existe la carta")
        return ResponseEntity.ok(card)
    }

    /**
     * Devuelve las cartas pertenecientes exclusivamente al usuario autenticado.
     */
    @GetMapping("/user/my-cards")
    fun getMyCardsSummary(
        @AuthenticationPrincipal principal: AuthenticatedUser,
    ): ResponseEntity<List<CardSummaryResponse>> {
        val cards = getCardUseCase.getByOwner(principal.id)
        return ResponseEntity.ok(cards.map { CardSummaryResponse.fromDomain(it) })
    }

    @DeleteMapping("/{id}")
    fun deleteCard(
        @PathVariable id: UUID,
        @AuthenticationPrincipal principal: AuthenticatedUser,
    ): ResponseEntity<Void> {
        deleteCardUseCase.execute(id, principal.id)
        return ResponseEntity.noContent().build()
    }

    @PutMapping("/{id}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun updateCard(
        @PathVariable id: UUID,
        @AuthenticationPrincipal principal: AuthenticatedUser,
        @RequestPart("data") dataJson: String,
        @RequestPart("image", required = false) imageFile: MultipartFile?,
    ): ResponseEntity<Card> {
        val request = objectMapper.readValue(dataJson, UpdateCardRequest::class.java)

        val existingCard =
            getCardUseCase.getById(id)
                ?: throw IllegalArgumentException("Carta no existe")

        if (request.category != null) {
            require(CardCatalog.isValid(request.category, request.collection ?: existingCard.collection)) {
                "La categoría o colección enviada no es válida"
            }
        }

        val imageUrl =
            if (imageFile != null) {
                fileStoragePort.store(imageFile)
            } else {
                existingCard.image
            }

        val updatedData =
            existingCard.copy(
                name = request.name ?: existingCard.name,
                category = request.category ?: existingCard.category,
                collection = request.collection ?: existingCard.collection,
                num = request.num ?: existingCard.num,
                state = request.state ?: existingCard.state,
                description = request.description ?: existingCard.description,
                image = imageUrl,
            )

        val result = updateCardUseCase.execute(id, principal.id, updatedData)
        return ResponseEntity.ok(result)
    }
}
