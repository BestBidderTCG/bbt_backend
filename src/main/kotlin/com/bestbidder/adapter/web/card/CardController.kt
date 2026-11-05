package com.bestbidder.adapter.web.card

import com.bestbidder.adapter.web.card.dto.CreateCardRequest
import com.bestbidder.domain.card.Card
import com.bestbidder.infrastructure.security.AuthenticatedUser
import com.bestbidder.usecase.card.CreateCardUseCase
import com.bestbidder.usecase.card.GetCardUseCase
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
@RequestMapping("/api/card")
class CardController(
    private val createCardUseCase: CreateCardUseCase,
    private val getCardUseCase: GetCardUseCase,
) {
    @PostMapping("/create")
    fun createCard(
        @AuthenticationPrincipal principal: AuthenticatedUser,
        @RequestBody request: CreateCardRequest,
    ): ResponseEntity<Card> {
        val card =
            createCardUseCase.execute(
                ownerId = principal.id,
                name = request.name,
                image = request.image,
                category = request.category,
                collection = request.collection,
                num = request.num,
                state = request.state,
                description = request.description,
            )

        return ResponseEntity.status(HttpStatus.CREATED).body(card)
    }

    @GetMapping("/all")
    fun getAllCards(): ResponseEntity<List<Card>> {
        val cards = getCardUseCase.getAll()
        return ResponseEntity.ok(cards)
    }

    @GetMapping("/{id}")
    fun getCardById(
        @PathVariable id: UUID,
    ): ResponseEntity<Card> {
        val card =
            getCardUseCase.getById(id)
                ?: throw IllegalArgumentException("No existe ninguna carta con ID $id")
        return ResponseEntity.ok(card)
    }

    @GetMapping("/user/my-cards")
    fun getMyCards(
        @AuthenticationPrincipal principal: AuthenticatedUser,
    ): ResponseEntity<List<Card>> {
        val cards = getCardUseCase.getByOwner(principal.id)
        return ResponseEntity.ok(cards)
    }
}
