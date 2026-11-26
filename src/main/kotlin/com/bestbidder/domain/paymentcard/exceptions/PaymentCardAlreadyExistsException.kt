package com.bestbidder.domain.paymentcard.exceptions

import java.util.UUID

class PaymentCardAlreadyExistsException(userId: UUID) :
    RuntimeException("User $userId already has a payment card")
