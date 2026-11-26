package com.bestbidder.usecase.paymentcard

import com.bestbidder.domain.paymentcard.PaymentCard
import com.bestbidder.domain.paymentcard.PaymentCardValidator
import com.bestbidder.domain.paymentcard.exceptions.PaymentCardAlreadyExistsException
import com.bestbidder.usecase.port.UserRepository
import com.bestbidder.usecase.user.GetUserUseCase
import org.springframework.stereotype.Service
import java.util.UUID

data class RegisterPaymentCardCommand(
    val userId: UUID,
    val cardNumber: String,
    val holderName: String,
    val expiryMonth: Int,
    val expiryYear: Int,
    val cvv: String,
)

@Service
class RegisterPaymentCardUseCase(
    private val userRepository: UserRepository,
    private val getUserUseCase: GetUserUseCase,
) {
    fun execute(command: RegisterPaymentCardCommand): PaymentCard {
        val user = getUserUseCase.getById(command.userId)
        if (user.paymentCard != null) throw PaymentCardAlreadyExistsException(command.userId)

        PaymentCardValidator.validate(
            cardNumber = command.cardNumber,
            holderName = command.holderName,
            expiryMonth = command.expiryMonth,
            expiryYear = command.expiryYear,
            cvv = command.cvv,
        )

        val card =
            PaymentCard(
                holderName = command.holderName.trim(),
                last4 = command.cardNumber.takeLast(PaymentCard.LAST4_LENGTH),
                expiryMonth = command.expiryMonth,
                expiryYear = command.expiryYear,
            )

        userRepository.save(user.copy(paymentCard = card))
        return card
    }
}
