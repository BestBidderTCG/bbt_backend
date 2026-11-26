package com.bestbidder.adapter.web.wallet

import com.bestbidder.domain.bid.exceptions.BidOutbidException
import com.bestbidder.domain.wallet.exceptions.InsufficientBalanceException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class WalletExceptionHandler {
    @ExceptionHandler(InsufficientBalanceException::class)
    fun handleInsufficientBalance(e: InsufficientBalanceException): ResponseEntity<Map<String, String?>> =
        ResponseEntity.status(HttpStatus.CONFLICT).body(mapOf("error" to e.message))

    @ExceptionHandler(BidOutbidException::class)
    fun handleOutbid(e: BidOutbidException): ResponseEntity<Map<String, String?>> =
        ResponseEntity.status(HttpStatus.CONFLICT).body(mapOf("error" to e.message))
}
