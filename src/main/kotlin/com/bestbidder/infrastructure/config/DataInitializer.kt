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

private const val CLOUDINARY_BASE = "https://res.cloudinary.com/ee6azugw/image/upload/bestbidder"

@Configuration
class DataInitializer(
    private val userRepository: JpaUserRepository,
    private val cardRepository: JpaCardRepository,
    private val passwordEncoder: PasswordEncoder,
) : CommandLineRunner {
    private fun cardImageUrl(id: String): String = "$CLOUDINARY_BASE/$id.jpg"

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
                        id = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
                        owner = savedUser1,
                        name = "Charizard ex",
                        image = cardImageUrl("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
                        category = "Pokémon",
                        collection = "Pokémon 151",
                        num = 6,
                        state = CardState.NEAR_MINT,
                        description = "Carta mítica en excelente estado.",
                    ),
                    CardEntity(
                        id = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"),
                        owner = savedUser1,
                        name = "Black Lotus",
                        image = cardImageUrl("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"),
                        category = "Magic",
                        collection = "Alpha / Beta / Unlimited",
                        num = 232,
                        state = CardState.GOOD,
                        description = "Joya de la Lista Reservada con ligero desgaste en los bordes.",
                    ),
                    CardEntity(
                        id = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"),
                        owner = savedUser1,
                        name = "Jinx - Loose Cannon",
                        image = cardImageUrl("cccccccc-cccc-cccc-cccc-cccccccccccc"),
                        category = "Riftbound",
                        collection = "Riftbound: Origins",
                        num = 301,
                        state = CardState.NEW,
                        description =
                            "Al inicio de tu fase inicial, roba 1 carta si tienes " +
                                "una o menos cartas en tu mano.",
                    ),
                    CardEntity(
                        id = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd"),
                        owner = savedUser2,
                        name = "Pikachu ex",
                        image = cardImageUrl("dddddddd-dddd-dddd-dddd-dddddddddddd"),
                        category = "Pokémon",
                        collection = "Surging Sparks",
                        num = 238,
                        state = CardState.NEAR_MINT,
                        description =
                            "Habilidad Corazón Resuelto: si tiene todos sus PS y quedaría " +
                                "Fuera de Combate, conserva 10 PS. Rayo Topacio: 300 de daño.",
                    ),
                    CardEntity(
                        id = UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee"),
                        owner = savedUser2,
                        name = "Mox Diamond",
                        image = cardImageUrl("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee"),
                        category = "Magic",
                        collection = "Ciclo de Urza (1998-1999)",
                        num = 138,
                        state = CardState.LIGHT_PLAYED,
                        description =
                            "Al entrar al campo, descarta una tierra o sacrifícalo. " +
                                "Girar: añade un maná de cualquier color a tu reserva.",
                    ),
                    CardEntity(
                        id = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff"),
                        owner = savedUser2,
                        name = "Sett - The Boss",
                        image = cardImageUrl("ffffffff-ffff-ffff-ffff-ffffffffffff"),
                        category = "Riftbound",
                        collection = "Riftbound: Origins",
                        num = 310,
                        state = CardState.GOOD,
                        description =
                            "Cuando una unidad potenciada que controlas vaya a morir, " +
                                "puedes agotarme para devolverla agotada a tu base.",
                    ),
                    CardEntity(
                        id = UUID.fromString("77777777-7777-7777-7777-777777777777"),
                        owner = savedUser3,
                        name = "Umbreon ex",
                        image = cardImageUrl("77777777-7777-7777-7777-777777777777"),
                        category = "Pokémon",
                        collection = "Destinos de Paldea",
                        num = 17,
                        state = CardState.POOR,
                        description = "Con pliegues visibles en la parte posterior.",
                    ),
                    CardEntity(
                        id = UUID.fromString("88888888-8888-8888-8888-888888888888"),
                        owner = savedUser3,
                        name = "Ancestral Recall",
                        image = cardImageUrl("88888888-8888-8888-8888-888888888888"),
                        category = "Magic",
                        collection = "Alpha / Beta / Unlimited",
                        num = 48,
                        state = CardState.LIGHT_PLAYED,
                        description = "Carta histórica del Power 9 en buen estado jugable.",
                    ),
                    CardEntity(
                        id = UUID.fromString("99999999-9999-9999-9999-999999999999"),
                        owner = savedUser3,
                        name = "Teemo - Swift Scout",
                        image = cardImageUrl("99999999-9999-9999-9999-999999999999"),
                        category = "Riftbound",
                        collection = "Riftbound: Origins",
                        num = 307,
                        state = CardState.NEAR_MINT,
                        description =
                            "Puedes pagar 1 para ocultar una carta con Oculto. " +
                                "Agotar: devuelve una unidad Teemo que controles a tu mano.",
                    ),
                )

            cardRepository.saveAll(cards)
        }
    }
}
