package com.bestbidder.adapter.web.paymentcard.dto

data class PaymentCardRequest(
    val cardNumber: String,
    val holderName: String,
    val expiryMonth: Int,
    val expiryYear: Int,
    val cvv: String,
) {
    override fun toString(): String = "PaymentCardRequest(holderName=$holderName)"
}
