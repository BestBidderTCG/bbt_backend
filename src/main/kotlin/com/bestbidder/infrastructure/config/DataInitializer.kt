package com.bestbidder.infrastructure.config

import com.bestbidder.adapter.persistence.user.JpaUserRepository
import com.bestbidder.adapter.persistence.user.UserEntity
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class DataInitializer(
    private val jpaUserRepository: JpaUserRepository
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        if (jpaUserRepository.count() == 0L) {
            val testUsers = listOf(
                UserEntity(
                    id = UUID.fromString("11111111-1111-1111-1111-111111111111"),
                    username = "user1",
                    passwordHash = "\$2a\$10\$3z.nKk35o8e7JjLzYgUa3eS9sX1X0a.Y.a7a1W7M7T/9F3a5Y.8mG",
                    displayName = "Usuario Uno",
                    description = "Coleccionista principal",
                    avatarNum = "1"
                ),
                UserEntity(
                    id = UUID.fromString("22222222-2222-2222-2222-222222222222"),
                    username = "user2",
                    passwordHash = "\$2a\$10\$wN1Q6m8L9z5fJ4K3H2G1FeX0Y.a7a1W7M7T/9F3a5Y.8mG7K2J1a.",
                    displayName = "Usuario Dos",
                    description = "Pujador habitual",
                    avatarNum = "2"
                ),
                UserEntity(
                    id = UUID.fromString("33333333-3333-3333-3333-333333333333"),
                    username = "user3",
                    passwordHash = "\$2a\$10\$k8M7N6P5Q4R3S2T1U0V9WeX0Y.a7a1W7M7T/9F3a5Y.8mG7K2J1a.",
                    displayName = "Usuario Tres",
                    description = "Vendedor TCG",
                    avatarNum = "3"
                )
            )
            jpaUserRepository.saveAll(testUsers)
            println("Usuarios de prueba insertados en PostgreSQL correctamente.")
        }
    }
}