package com.bestbidder.adapter.web.paymentcard

import com.bestbidder.domain.paymentcard.exceptions.InvalidPaymentCardException
import com.bestbidder.domain.paymentcard.exceptions.PaymentCardAlreadyExistsException
import com.bestbidder.domain.paymentcard.exceptions.PaymentCardNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class PaymentCardExceptionHandler {
    @ExceptionHandler(InvalidPaymentCardException::class)
    fun handleInvalid(e: InvalidPaymentCardException): ResponseEntity<Map<String, String?>> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to e.message))

    @ExceptionHandler(PaymentCardAlreadyExistsException::class)
    fun handleAlreadyExists(e: PaymentCardAlreadyExistsException): ResponseEntity<Map<String, String?>> =
        ResponseEntity.status(HttpStatus.CONFLICT).body(mapOf("error" to e.message))

    @ExceptionHandler(PaymentCardNotFoundException::class)
    fun handleNotFound(e: PaymentCardNotFoundException): ResponseEntity<Map<String, String?>> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to e.message))
}
