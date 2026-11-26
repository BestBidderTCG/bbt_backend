package com.bestbidder.adapter.persistence.paymentcard

import com.bestbidder.adapter.persistence.user.UserEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "payment_card")
class PaymentCardEntity(
    @Id
    val id: UUID,
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    val user: UserEntity,
    @Column(name = "holder_name", nullable = false)
    val holderName: String,
    @Column(nullable = false, length = 4)
    val last4: String,
    @Column(name = "expiry_month", nullable = false)
    val expiryMonth: Int,
    @Column(name = "expiry_year", nullable = false)
    val expiryYear: Int,
)
