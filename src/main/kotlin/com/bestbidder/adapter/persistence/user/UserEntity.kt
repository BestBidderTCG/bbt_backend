package com.bestbidder.adapter.persistence.user

import com.bestbidder.adapter.persistence.paymentcard.PaymentCardEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.time.Instant
import java.time.LocalDateTime
import java.util.UUID

/**
 * Representación de la tabla "user" en PostgreSQL.
 * Para que JPA gestione la persistencia automáticamente.
 */
@Entity
@Table(name = "bidder")
@Suppress("LongParameterList")
class UserEntity(
    @Id val id: UUID,
    @Column(unique = true, nullable = false, length = 100)
    val email: String,
    @Column(unique = true, nullable = false, length = 50)
    val username: String,
    @Column(name = "password_hash", nullable = false)
    val passwordHash: String,
    @Column(name = "description", length = 500)
    val description: String?,
    @Column(name = "avatar_num")
    val avatarNum: Int?,
    @Column(name = "created_at", updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @Column(name = "deletion_requested_at")
    val deletionRequestedAt: Instant? = null,
    @Column(name = "deleted_at")
    val deletedAt: Instant? = null,
    @Column(nullable = false, columnDefinition = "double precision not null default 0")
    val balance: Double = 0.0,
) {
    @OneToOne(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    var paymentCard: PaymentCardEntity? = null
}
