package com.bestbidder.adapter.persistence.user

import com.bestbidder.domain.user.User
import com.bestbidder.usecase.port.UserRepository
import org.springframework.stereotype.Component
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

    override fun existsByUsername(username: String): Boolean = jpaUserRepository.existsByUsername(username)

    override fun save(user: User): User = jpaUserRepository.save(user.toEntity()).toDomain()

    // Mappers
    private fun UserEntity.toDomain() =
        User(
            id = id,
            username = username,
            passwordHash = passwordHash,
            displayName = displayName,
            description = description,
            avatarNum = avatarNum,
            createdAt = createdAt,
        )

    private fun User.toEntity() =
        UserEntity(
            id = id,
            username = username,
            passwordHash = passwordHash,
            displayName = displayName,
            description = description,
            avatarNum = avatarNum,
            createdAt = createdAt,
        )
}
