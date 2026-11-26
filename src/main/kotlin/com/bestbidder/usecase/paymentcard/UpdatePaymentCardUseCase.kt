package com.bestbidder.usecase.paymentcard

import com.bestbidder.domain.paymentcard.PaymentCard
import com.bestbidder.domain.paymentcard.PaymentCardValidator
import com.bestbidder.domain.paymentcard.exceptions.PaymentCardNotFoundException
import com.bestbidder.usecase.port.UserRepository
import com.bestbidder.usecase.user.GetUserUseCase
import org.springframework.stereotype.Service
import java.util.UUID

data class UpdatePaymentCardCommand(
    val userId: UUID,
    val cardNumber: String,
    val holderName: String,
    val expiryMonth: Int,
    val expiryYear: Int,
    val cvv: String,
)

@Service
class UpdatePaymentCardUseCase(
    private val userRepository: UserRepository,
    private val getUserUseCase: GetUserUseCase,
) {
    fun execute(command: UpdatePaymentCardCommand): PaymentCard {
        val user = getUserUseCase.getById(command.userId)
        val existing = user.paymentCard ?: throw PaymentCardNotFoundException(command.userId)

        PaymentCardValidator.validate(
            cardNumber = command.cardNumber,
            holderName = command.holderName,
            expiryMonth = command.expiryMonth,
            expiryYear = command.expiryYear,
            cvv = command.cvv,
        )

        val updated =
            existing.copy(
                holderName = command.holderName.trim(),
                last4 = command.cardNumber.takeLast(PaymentCard.LAST4_LENGTH),
                expiryMonth = command.expiryMonth,
                expiryYear = command.expiryYear,
            )

        userRepository.save(user.copy(paymentCard = updated))
        return updated
    }
}
