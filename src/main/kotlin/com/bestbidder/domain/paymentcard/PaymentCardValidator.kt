package com.bestbidder.domain.paymentcard

import com.bestbidder.domain.paymentcard.exceptions.InvalidPaymentCardException
import java.time.YearMonth

object PaymentCardValidator {
    private const val CARD_NUMBER_LENGTH = 16
    private const val CVV_LENGTH = 3
    private const val MIN_YEAR = 2000
    private const val MAX_YEAR = 2100

    fun validate(
        cardNumber: String,
        holderName: String,
        expiryMonth: Int,
        expiryYear: Int,
        cvv: String,
    ) {
        ensure(holderName.isNotBlank(), "Holder name is required")
        ensure(isDigits(cardNumber, CARD_NUMBER_LENGTH), "Card number must be 16 digits")
        ensure(isDigits(cvv, CVV_LENGTH), "CVV must be 3 digits")
        ensure(
            expiryMonth in PaymentCard.VALID_MONTHS && expiryYear in MIN_YEAR..MAX_YEAR,
            "Invalid expiry date",
        )
        ensure(!YearMonth.of(expiryYear, expiryMonth).isBefore(YearMonth.now()), "Card is expired")
    }

    private fun isDigits(
        value: String,
        length: Int,
    ) = value.length == length && value.all { it.isDigit() }

    private fun ensure(
        condition: Boolean,
        message: String,
    ) {
        if (!condition) throw InvalidPaymentCardException(message)
    }
}
