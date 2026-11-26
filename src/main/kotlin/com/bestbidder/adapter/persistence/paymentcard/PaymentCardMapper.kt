package com.bestbidder.adapter.persistence.paymentcard

import com.bestbidder.adapter.persistence.user.UserEntity
import com.bestbidder.domain.paymentcard.PaymentCard

fun PaymentCardEntity.toDomain() =
    PaymentCard(
        id = id,
        holderName = holderName,
        last4 = last4,
        expiryMonth = expiryMonth,
        expiryYear = expiryYear,
    )

fun PaymentCard.toEntity(owner: UserEntity) =
    PaymentCardEntity(
        id = id,
        user = owner,
        holderName = holderName,
        last4 = last4,
        expiryMonth = expiryMonth,
        expiryYear = expiryYear,
    )
