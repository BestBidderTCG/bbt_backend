package com.bestbidder.usecase.auction

import com.bestbidder.domain.auction.Auction
import com.bestbidder.domain.card.Card
import com.bestbidder.domain.user.exceptions.UserNotFoundException
import com.bestbidder.usecase.port.AuctionRepository
import com.bestbidder.usecase.port.UserRepository
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.UUID

@Service
class CreateAuctionUseCase(
    private val auctionRepository: AuctionRepository,
    private val userRepository: UserRepository,
) {
    private companion object {
        const val MIN_DURATION_DAYS = 1
        const val MINUTE_BLOCK_ALIGNMENT = 15
    }

    @Suppress("LongParameterList")
    fun create(
        creatorId: UUID,
        creatorUsername: String,
        card: Card,
        description: String? = null,
        minPrize: Double,
        startedAt: Instant,
        durationInDays: Int,
    ): Auction {
        val now = Instant.now().truncatedTo(ChronoUnit.MINUTES)

        require(!startedAt.isBefore(now)) {
            "La fecha de inicio de la subasta no puede ser anterior al instante actual"
        }

        val creator = userRepository.findById(creatorId) ?: throw UserNotFoundException(creatorId)
        check(creator.deletedAt == null && !creator.isPendingDeletion) {
            "Tu cuenta está pendiente de eliminación. Inicia sesión para reactivarla antes de operar."
        }

        val zonedDateTime = startedAt.atZone(ZoneOffset.UTC)
        val minute = zonedDateTime.minute
        val second = zonedDateTime.second
        val nano = zonedDateTime.nano

        require(minute % MINUTE_BLOCK_ALIGNMENT == 0 && second == 0 && nano == 0) {
            "La hora de activación debe estar alineada a bloques de 15 minutos exactos (ej. :00, :15, :30, :45)"
        }

        require(durationInDays >= MIN_DURATION_DAYS) {
            "La duración de la subasta debe ser de al menos 1 día"
        }

        val exists = auctionRepository.existsByCreatorAndCard(creatorUsername, card.id)
        require(!exists) {
            "Ya existe una subasta activa o próxima para la carta con ID '${card.id}' y el usuario '$creatorUsername'"
        }

        val cleanStartedAt = startedAt.truncatedTo(ChronoUnit.MINUTES)

        val newAuction =
            Auction(
                creatorId = creatorId,
                creatorUsername = creatorUsername,
                card = card,
                description = description,
                minPrize = minPrize,
                startedAt = cleanStartedAt,
                durationInDays = durationInDays,
            )

        return auctionRepository.save(newAuction)
    }
}
