package com.bestbidder.adapter.persistence.user

import com.bestbidder.adapter.persistence.paymentcard.toDomain
import com.bestbidder.adapter.persistence.paymentcard.toEntity
import com.bestbidder.domain.user.User
import com.bestbidder.usecase.port.UserRepository
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID

/**
 * Adaptador que implementa la interfaz.
 * Se encarga de conectar la lógica con la base de datos real.
 * También actúa como "Traductor" (Mapper) entre [UserEntity] (DB) y [User] (Dominio).
 */
@Component
class UserMapper(
    private val jpaUserRepository: JpaUserRepository,
) : UserRepository {
    override fun findById(id: UUID): User? = jpaUserRepository.findById(id).orElse(null)?.toDomain()

    override fun findByUsername(username: String): User? = jpaUserRepository.findByUsername(username)?.toDomain()

    override fun findByEmail(email: String): User? = jpaUserRepository.findByEmail(email)?.toDomain()

    override fun existsByUsername(username: String): Boolean = jpaUserRepository.existsByUsername(username)

    override fun existsByEmail(email: String): Boolean = jpaUserRepository.existsByEmail(email)

    override fun save(user: User): User = jpaUserRepository.save(user.toEntity()).toDomain()

    override fun findPendingDeletionBefore(cutoff: Instant): List<User> =
        jpaUserRepository.findByDeletionRequestedAtBeforeAndDeletedAtIsNull(cutoff).map { it.toDomain() }

    override fun deductBalance(
        userId: UUID,
        amount: Double,
    ): Double? {
        val updatedRows = jpaUserRepository.deductBalance(userId, amount)
        return if (updatedRows == 0) null else jpaUserRepository.findBalanceById(userId)
    }

    override fun addBalance(
        userId: UUID,
        amount: Double,
    ): Double {
        jpaUserRepository.addBalance(userId, amount)
        return jpaUserRepository.findBalanceById(userId) ?: 0.0
    }
}

private fun UserEntity.toDomain() =
    User(
        id = id,
        email = email,
        username = username,
        passwordHash = passwordHash,
        description = description,
        avatarNum = avatarNum,
        createdAt = createdAt,
        deletionRequestedAt = deletionRequestedAt,
        deletedAt = deletedAt,
        paymentCard = paymentCard?.toDomain(),
        balance = balance,
    )

private fun User.toEntity(): UserEntity {
    val entity =
        UserEntity(
            id = id,
            email = email,
            username = username,
            passwordHash = passwordHash,
            description = description,
            avatarNum = avatarNum,
            createdAt = createdAt,
            deletionRequestedAt = deletionRequestedAt,
            deletedAt = deletedAt,
            balance = balance,
        )
    entity.paymentCard = paymentCard?.toEntity(entity)
    return entity
}
