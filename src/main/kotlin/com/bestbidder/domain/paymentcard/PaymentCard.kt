package com.bestbidder.domain.paymentcard

import java.util.UUID

data class PaymentCard(
    val id: UUID = UUID.randomUUID(),
    val holderName: String,
    val last4: String,
    val expiryMonth: Int,
    val expiryYear: Int,
) {
    init {
        require(last4.length == LAST4_LENGTH && last4.all { it.isDigit() }) { "last4 must be 4 digits" }
        require(expiryMonth in VALID_MONTHS) { "expiryMonth must be between 1 and 12" }
    }

    fun masked(): String = "**** **** **** $last4"

    companion object {
        const val LAST4_LENGTH = 4
        val VALID_MONTHS = 1..12
    }
}
