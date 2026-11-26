package com.bestbidder.usecase.paymentcard

import com.bestbidder.domain.paymentcard.PaymentCard
import com.bestbidder.domain.paymentcard.exceptions.PaymentCardNotFoundException
import com.bestbidder.usecase.user.GetUserUseCase
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class GetPaymentCardUseCase(
    private val getUserUseCase: GetUserUseCase,
) {
    fun execute(userId: UUID): PaymentCard =
        getUserUseCase.getById(userId).paymentCard
            ?: throw PaymentCardNotFoundException(userId)
}
