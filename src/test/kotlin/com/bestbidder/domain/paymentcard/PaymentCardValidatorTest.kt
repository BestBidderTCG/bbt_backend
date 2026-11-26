package com.bestbidder.domain.paymentcard

import com.bestbidder.domain.paymentcard.exceptions.InvalidPaymentCardException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.YearMonth

class PaymentCardValidatorTest {
    private val nextYear = YearMonth.now().year + 1

    private fun validate(
        cardNumber: String = "1234123412341234",
        holderName: String = "Paula",
        expiryMonth: Int = 12,
        expiryYear: Int = nextYear,
        cvv: String = "123",
    ) = PaymentCardValidator.validate(cardNumber, holderName, expiryMonth, expiryYear, cvv)

    private fun assertInvalid(
        expectedMessage: String,
        block: () -> Unit,
    ) {
        val exception = assertThrows(InvalidPaymentCardException::class.java) { block() }
        assertEquals(expectedMessage, exception.message)
    }

    @Test
    @DisplayName("Acepta una tarjeta con datos válidos")
    fun `should accept valid card`() {
        validate()
    }

    @Test
    @DisplayName("Acepta una tarjeta que caduca este mismo mes")
    fun `should accept card expiring this month`() {
        val now = YearMonth.now()
        validate(expiryMonth = now.monthValue, expiryYear = now.year)
    }

    @Test
    fun `should reject blank holder name`() = assertInvalid("Holder name is required") { validate(holderName = "  ") }

    @Test
    fun `should reject card number with wrong length`() =
        assertInvalid("Card number must be 16 digits") { validate(cardNumber = "123412341234") }

    @Test
    fun `should reject card number with letters`() =
        assertInvalid("Card number must be 16 digits") { validate(cardNumber = "1234abcd12341234") }

    @Test
    fun `should reject cvv with wrong length`() = assertInvalid("CVV must be 3 digits") { validate(cvv = "12") }

    @Test
    fun `should reject invalid month`() = assertInvalid("Invalid expiry date") { validate(expiryMonth = 13) }

    @Test
    @DisplayName("Rechaza una tarjeta caducada el mes pasado")
    fun `should reject expired card`() {
        val lastMonth = YearMonth.now().minusMonths(1)
        assertInvalid("Card is expired") {
            validate(expiryMonth = lastMonth.monthValue, expiryYear = lastMonth.year)
        }
    }
}
