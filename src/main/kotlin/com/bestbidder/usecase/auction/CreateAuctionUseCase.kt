package com.bestbidder.usecase.auction

import com.bestbidder.domain.auction.Auction
import com.bestbidder.domain.card.Card
import com.bestbidder.usecase.port.AuctionRepository
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.UUID

@Service
class CreateAuctionUseCase(
    private val auctionRepository: AuctionRepository,
) {
    fun create(
        creator_id: UUID,
        creator_username: String,
        card: Card,
        description: String? = null,
        minPrize: Double,
        startedAt: Instant,
        durationInDays: Int,
    ): Auction {
        val now = Instant.now().truncatedTo(ChronoUnit.MINUTES)

        if (startedAt.isBefore(now)) {
            throw IllegalArgumentException("La fecha de inicio de la subasta no puede ser anterior al instante actual")
        }

        val zonedDateTime = startedAt.atZone(ZoneOffset.UTC)
        val minute = zonedDateTime.minute
        val second = zonedDateTime.second
        val nano = zonedDateTime.nano

        if (minute % 15 != 0 || second != 0 || nano != 0) {
            throw IllegalArgumentException(
                "La hora de activación debe estar alineada a bloques de 15 minutos exactos (ej. :00, :15, :30, :45)",
            )
        }

        if (durationInDays < 1) {
            throw IllegalArgumentException("La duración de la subasta debe ser de al menos 1 día")
        }

        val exists = auctionRepository.existsByCreatorAndCard(creator_username, card.id)
        if (exists) {
            throw IllegalArgumentException(
                "Ya existe una subasta activa o próxima para la carta con ID '${card.id}' y el usuario '$creator_username'",
            )
        }

        val cleanStartedAt = startedAt.truncatedTo(ChronoUnit.MINUTES)

        val newAuction =
            Auction(
                creator_id = creator_id,
                creator_username = creator_username,
                card = card,
                description = description,
                min_prize = minPrize,
                startedAt = cleanStartedAt,
                durationInDays = durationInDays,
            )

        return auctionRepository.save(newAuction)
    }
}
