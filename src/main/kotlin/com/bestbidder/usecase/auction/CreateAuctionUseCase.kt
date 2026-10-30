package com.bestbidder.usecase.auction

import com.bestbidder.domain.auction.Auction
import com.bestbidder.usecase.port.AuctionRepository
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@Service
class CreateAuctionUseCase(
    private val auctionRepository: AuctionRepository,
) {
    fun create(
        creator_id: UUID,
        creator_username: String,
        card: String,
        collection: String,
        minPrize: Double,
        startedAt: Instant,
        durationInDays: Int,
    ): Auction {
        val exists = auctionRepository.existsByCreatorAndCard(creator_username, card)
        if (exists) {
            throw IllegalArgumentException("Ya existe una subasta activa o próxima de la carta '$card' para el usuario '$creator_username'")
        }

        // Se limpian segundos y milisegundos a :00.000Z
        val cleanStartedAt = startedAt.truncatedTo(ChronoUnit.MINUTES)

        val newAuction =
            Auction(
                creator_id = creator_id,
                creator_username = creator_username,
                card = card,
                collection = collection,
                min_prize = minPrize,
                startedAt = cleanStartedAt,
                durationInDays = durationInDays,
            )

        return auctionRepository.save(newAuction)
    }
}
