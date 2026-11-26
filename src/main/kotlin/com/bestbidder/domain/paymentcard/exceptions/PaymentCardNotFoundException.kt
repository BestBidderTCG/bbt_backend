package com.bestbidder.domain.paymentcard.exceptions

import java.util.UUID

class PaymentCardNotFoundException(userId: UUID) :
    RuntimeException("User $userId has no payment card")
