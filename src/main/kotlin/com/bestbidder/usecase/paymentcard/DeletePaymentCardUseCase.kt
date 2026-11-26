package com.bestbidder.usecase.paymentcard

import com.bestbidder.domain.paymentcard.exceptions.PaymentCardNotFoundException
import com.bestbidder.usecase.port.UserRepository
import com.bestbidder.usecase.user.GetUserUseCase
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class DeletePaymentCardUseCase(
    private val userRepository: UserRepository,
    private val getUserUseCase: GetUserUseCase,
) {
    fun execute(userId: UUID) {
        val user = getUserUseCase.getById(userId)
        if (user.paymentCard == null) throw PaymentCardNotFoundException(userId)

        userRepository.save(user.copy(paymentCard = null))
    }
}
