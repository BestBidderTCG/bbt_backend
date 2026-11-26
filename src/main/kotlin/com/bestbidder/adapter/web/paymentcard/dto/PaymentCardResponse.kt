package com.bestbidder.adapter.web.paymentcard.dto

import com.bestbidder.domain.paymentcard.PaymentCard

data class PaymentCardResponse(
    val holderName: String,
    val maskedNumber: String,
    val expiryMonth: Int,
    val expiryYear: Int,
) {
    companion object {
        fun fromDomain(card: PaymentCard) =
            PaymentCardResponse(
                holderName = card.holderName,
                maskedNumber = card.masked(),
                expiryMonth = card.expiryMonth,
                expiryYear = card.expiryYear,
            )
    }
}
