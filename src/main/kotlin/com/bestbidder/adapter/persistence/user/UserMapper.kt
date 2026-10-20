package com.bestbidder.adapter.persistence.user

import com.bestbidder.domain.user.User
import com.bestbidder.usecase.port.UserRepository
import org.springframework.stereotype.Component

/**
 * Adaptador que implementa la interfaz.
 * Se encarga de conectar la lógica con la base de datos real.
 * También actúa como "Traductor" (Mapper) entre [UserEntity] (DB) y [User] (Dominio).
 */
@Component
class UserPersistenceAdapter(
    private val jpaUserRepository: JpaUserRepository
) : UserRepository {

    override fun findByUsername(username: String): User? {
        return jpaUserRepository.findByUsername(username)?.toDomain()
    }

    override fun save(user: User): User {
        val entity = jpaUserRepository.save(user.toEntity())
        return entity.toDomain()
    }

    // Mappers
    private fun UserEntity.toDomain() = User(id, username, passwordHash)
    private fun User.toEntity() = UserEntity(id, username, passwordHash)
}