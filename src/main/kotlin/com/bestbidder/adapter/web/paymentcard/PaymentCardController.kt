package com.bestbidder.adapter.web.paymentcard

import com.bestbidder.adapter.web.paymentcard.dto.PaymentCardRequest
import com.bestbidder.adapter.web.paymentcard.dto.PaymentCardResponse
import com.bestbidder.infrastructure.security.AuthenticatedUser
import com.bestbidder.usecase.paymentcard.DeletePaymentCardUseCase
import com.bestbidder.usecase.paymentcard.GetPaymentCardUseCase
import com.bestbidder.usecase.paymentcard.RegisterPaymentCardCommand
import com.bestbidder.usecase.paymentcard.RegisterPaymentCardUseCase
import com.bestbidder.usecase.paymentcard.UpdatePaymentCardCommand
import com.bestbidder.usecase.paymentcard.UpdatePaymentCardUseCase
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/payment-card")
class PaymentCardController(
    private val registerPaymentCard: RegisterPaymentCardUseCase,
    private val updatePaymentCard: UpdatePaymentCardUseCase,
    private val deletePaymentCard: DeletePaymentCardUseCase,
    private val getPaymentCard: GetPaymentCardUseCase,
) {
    private fun getCurrentUser(): AuthenticatedUser {
        val auth = SecurityContextHolder.getContext().authentication
        return auth.principal as AuthenticatedUser
    }

    @GetMapping
    fun get(): PaymentCardResponse = PaymentCardResponse.fromDomain(getPaymentCard.execute(getCurrentUser().id))

    @PostMapping
    fun register(
        @RequestBody req: PaymentCardRequest,
    ): ResponseEntity<PaymentCardResponse> {
        val card =
            registerPaymentCard.execute(
                RegisterPaymentCardCommand(
                    userId = getCurrentUser().id,
                    cardNumber = req.cardNumber,
                    holderName = req.holderName,
                    expiryMonth = req.expiryMonth,
                    expiryYear = req.expiryYear,
                    cvv = req.cvv,
                ),
            )
        return ResponseEntity.status(HttpStatus.CREATED).body(PaymentCardResponse.fromDomain(card))
    }

    @PutMapping
    fun update(
        @RequestBody req: PaymentCardRequest,
    ): PaymentCardResponse {
        val card =
            updatePaymentCard.execute(
                UpdatePaymentCardCommand(
                    userId = getCurrentUser().id,
                    cardNumber = req.cardNumber,
                    holderName = req.holderName,
                    expiryMonth = req.expiryMonth,
                    expiryYear = req.expiryYear,
                    cvv = req.cvv,
                ),
            )
        return PaymentCardResponse.fromDomain(card)
    }

    @DeleteMapping
    fun delete(): ResponseEntity<Void> {
        deletePaymentCard.execute(getCurrentUser().id)
        return ResponseEntity.noContent().build()
    }
}
