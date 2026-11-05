package com.bestbidder.infrastructure.config

import com.bestbidder.adapter.persistence.card.CardEntity
import com.bestbidder.adapter.persistence.card.JpaCardRepository
import com.bestbidder.adapter.persistence.user.JpaUserRepository
import com.bestbidder.adapter.persistence.user.UserEntity
import com.bestbidder.domain.card.CardState
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class DataInitializer(
    private val jpaUserRepository: JpaUserRepository,
    private val jpaCardRepository: JpaCardRepository,
) : CommandLineRunner {
    override fun run(vararg args: String?) {
        if (jpaUserRepository.count() == 0L) {
            val testUsers =
                listOf(
                    UserEntity(
                        id = UUID.randomUUID(),
                        username = "user1",
                        email = "user1@example.com",
                        passwordHash = "\$2a\$10\$3z.nKk35o8e7JjLzYgUa3eS9sX1X0a.Y.a7a1W7M7T/9F3a5Y.8mG",
                        description = "Coleccionista principal",
                        avatarNum = 1,
                    ),
                    UserEntity(
                        id = UUID.randomUUID(),
                        username = "user2",
                        email = "user2@example.com",
                        passwordHash = "\$2a\$10\$wN1Q6m8L9z5fJ4K3H2G1FeX0Y.a7a1W7M7T/9F3a5Y.8mG7K2J1a.",
                        description = "Pujador habitual",
                        avatarNum = 2,
                    ),
                    UserEntity(
                        id = UUID.randomUUID(),
                        username = "user3",
                        email = "user3@example.com",
                        passwordHash = "\$2a\$10\$k8M7N6P5Q4R3S2T1U0V9WeX0Y.a7a1W7M7T/9F3a5Y.8mG7K2J1a.",
                        description = "Vendedor TCG",
                        avatarNum = 3,
                    ),
                )

            val savedUsers = jpaUserRepository.saveAll(testUsers)
            val user1 = savedUsers[0]
            val user2 = savedUsers[1]
            val user3 = savedUsers[2]

            val testCards =
                listOf(
                    // Cartas User 1
                    CardEntity(
                        id = UUID.randomUUID(),
                        owner = user1,
                        name = "Charizard 1st Edition",
                        image = "charizard_base.png",
                        category = "Pokémon",
                        collection = "Base Set",
                        num = 4,
                        state = CardState.NEW,
                        description = "Carta holo impecable sin marcas de uso.",
                    ),
                    CardEntity(
                        id = UUID.randomUUID(),
                        owner = user1,
                        name = "Pikachu Illustrator",
                        image = "pikachu_illustrator.png",
                        category = "Pokémon",
                        collection = "Promo",
                        num = 1,
                        state = CardState.NEAR_MINT,
                        description = "Edición limitada de torneo en estuche protector.",
                    ),
                    CardEntity(
                        id = UUID.randomUUID(),
                        owner = user1,
                        name = "Blue-Eyes White Dragon",
                        image = "blue_eyes.png",
                        category = "Yu-Gi-Oh!",
                        collection = "LOB",
                        num = 1,
                        state = CardState.GOOD,
                        description = "Clásico de la primera edición LOB-001.",
                    ),
                    // Cartas User 2
                    CardEntity(
                        id = UUID.randomUUID(),
                        owner = user2,
                        name = "Black Lotus",
                        image = "black_lotus.png",
                        category = "Magic: The Gathering",
                        collection = "Alpha",
                        num = 232,
                        state = CardState.NEAR_MINT,
                        description = "Joyita vintage de Alpha Set en gran estado.",
                    ),
                    CardEntity(
                        id = UUID.randomUUID(),
                        owner = user2,
                        name = "Mewtwo Star",
                        image = "mewtwo_star.png",
                        category = "Pokémon",
                        collection = "EX Holon Phantoms",
                        num = 103,
                        state = CardState.LIGHT_PLAYED,
                        description = "Pequeño desgaste en la esquina inferior trasera.",
                    ),
                    CardEntity(
                        id = UUID.randomUUID(),
                        owner = user2,
                        name = "Dark Magician Girl",
                        image = "dark_magician_girl.png",
                        category = "Yu-Gi-Oh!",
                        collection = "MFC",
                        num = 0,
                        state = CardState.NEW,
                        description = "Secret Rare con brillo holo perfecto.",
                    ),
                    // Cartas User 3
                    CardEntity(
                        id = UUID.randomUUID(),
                        owner = user3,
                        name = "Luffy Gear 5 Super Rare",
                        image = "luffy_g5.png",
                        category = "One Piece TCG",
                        collection = "OP-05",
                        num = 119,
                        state = CardState.NEW,
                        description = "Recién sacada de sobre, impecable.",
                    ),
                    CardEntity(
                        id = UUID.randomUUID(),
                        owner = user3,
                        name = "Shanks Manga Alt Art",
                        image = "shanks_manga.png",
                        category = "One Piece TCG",
                        collection = "OP-01",
                        num = 120,
                        state = CardState.NEAR_MINT,
                        description = "Edición Manga Rare súper cotizada.",
                    ),
                    CardEntity(
                        id = UUID.randomUUID(),
                        owner = user3,
                        name = "Umbreon VMAX Gold Star",
                        image = "umbreon_vmax.png",
                        category = "Pokémon",
                        collection = "Evolving Skies",
                        num = 215,
                        state = CardState.POOR,
                        description = "Esquina superior derecha doblada.",
                    ),
                )

            jpaCardRepository.saveAll(testCards)
            println("Usuarios y cartas de prueba insertados en PostgreSQL correctamente.")
        }
    }
}
