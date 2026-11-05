package com.bestbidder.infrastructure.config

import com.bestbidder.adapter.persistence.card.CardEntity
import com.bestbidder.adapter.persistence.card.JpaCardRepository
import com.bestbidder.adapter.persistence.user.JpaUserRepository
import com.bestbidder.adapter.persistence.user.UserEntity
import com.bestbidder.domain.card.CardState
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.UUID

@Configuration
class DataInitializer(
    private val userRepository: JpaUserRepository,
    private val cardRepository: JpaCardRepository,
    private val passwordEncoder: PasswordEncoder,
) : CommandLineRunner {
    override fun run(vararg args: String?) {
        if (userRepository.count() == 0L) {
            val user1 =
                UserEntity(
                    id = UUID.fromString("11111111-1111-1111-1111-111111111111"),
                    username = "user1",
                    email = "user1@example.com",
                    passwordHash = passwordEncoder.encode("user11"),
                    description = "Coleccionista principal",
                    avatarNum = 1,
                )

            val user2 =
                UserEntity(
                    id = UUID.fromString("22222222-2222-2222-2222-222222222222"),
                    username = "user2",
                    email = "user2@example.com",
                    passwordHash = passwordEncoder.encode("user22"),
                    description = "Pujador habitual",
                    avatarNum = 2,
                )

            val user3 =
                UserEntity(
                    id = UUID.fromString("33333333-3333-3333-3333-333333333333"),
                    username = "user3",
                    email = "user3@example.com",
                    passwordHash = passwordEncoder.encode("user33"),
                    description = "Vendedor TCG",
                    avatarNum = 3,
                )

            val savedUser1 = userRepository.save(user1)
            val savedUser2 = userRepository.save(user2)
            val savedUser3 = userRepository.save(user3)

            val cards =
                listOf(
                    CardEntity(
                        id = UUID.randomUUID(),
                        owner = savedUser1,
                        name = "Charizard 1st Edition",
                        image = "charizard_base.png",
                        category = "Pokémon",
                        collection = "Base Set",
                        num = 4,
                        state = CardState.NEAR_MINT,
                        description = "Carta mítica en excelente estado.",
                    ),
                    CardEntity(
                        id = UUID.randomUUID(),
                        owner = savedUser1,
                        name = "Black Lotus",
                        image = "black_lotus.png",
                        category = "Magic: The Gathering",
                        collection = "Alpha",
                        num = 232,
                        state = CardState.GOOD,
                        description = "Joyero de reserva con ligero desgaste en bordes.",
                    ),
                    CardEntity(
                        id = UUID.randomUUID(),
                        owner = savedUser1,
                        name = "Blue-Eyes White Dragon",
                        image = "blue_eyes.png",
                        category = "Yu-Gi-Oh!",
                        collection = "LOB",
                        num = 1,
                        state = CardState.NEW,
                        description = "Primera edición en estado impecable.",
                    ),
                    CardEntity(
                        id = UUID.randomUUID(),
                        owner = savedUser2,
                        name = "Pikachu Illustrator",
                        image = "pikachu_illustrator.png",
                        category = "Pokémon",
                        collection = "Promo",
                        num = 0,
                        state = CardState.NEAR_MINT,
                        description = "Copia promocional japonesa muy rara.",
                    ),
                    CardEntity(
                        id = UUID.randomUUID(),
                        owner = savedUser2,
                        name = "Mox Diamond",
                        image = "mox_diamond.png",
                        category = "Magic: The Gathering",
                        collection = "Stronghold",
                        num = 138,
                        state = CardState.LIGHT_PLAYED,
                        description = "Usada en torneos, jugable sin funda doble.",
                    ),
                    CardEntity(
                        id = UUID.randomUUID(),
                        owner = savedUser2,
                        name = "Dark Magician",
                        image = "dark_magician.png",
                        category = "Yu-Gi-Oh!",
                        collection = "LOB",
                        num = 5,
                        state = CardState.GOOD,
                        description = "Clásico Mago Oscuro SDK.",
                    ),
                    CardEntity(
                        id = UUID.randomUUID(),
                        owner = savedUser3,
                        name = "Umbreon Star",
                        image = "umbreon_star.png",
                        category = "Pokémon",
                        collection = "POP Series 5",
                        num = 17,
                        state = CardState.POOR,
                        description = "Con pliegues visibles en la parte posterior.",
                    ),
                    CardEntity(
                        id = UUID.randomUUID(),
                        owner = savedUser3,
                        name = "Ancestral Recall",
                        image = "ancestral_recall.png",
                        category = "Magic: The Gathering",
                        collection = "Unlimited",
                        num = 48,
                        state = CardState.LIGHT_PLAYED,
                        description = "Carta histórica de Power 9.",
                    ),
                    CardEntity(
                        id = UUID.randomUUID(),
                        owner = savedUser3,
                        name = "Exodia the Forbidden One",
                        image = "exodia.png",
                        category = "Yu-Gi-Oh!",
                        collection = "LOB",
                        num = 124,
                        state = CardState.NEAR_MINT,
                        description = "Cabeza de Exodia en gran condición.",
                    ),
                )

            cardRepository.saveAll(cards)
        }
    }
}
