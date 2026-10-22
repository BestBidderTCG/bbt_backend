package com.bestbidder.usecase.auction

import com.bestbidder.domain.auction.Auction
import com.bestbidder.usecase.port.AuctionRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class CreateAuctionUseCase(
    private val auctionRepository: AuctionRepository,
) {
    fun create(
        creator: String,
        card: String,
        collection: String,
        minPrize: Double,
    ): Auction {
        val exists = auctionRepository.existsByCreatorAndCard(creator, card)
        if (exists) {
            throw IllegalArgumentException("Ya existe una subasta activa de la carta '$card' para el usuario '$creator'")
        }

        val newAuction =
            Auction(
                id = UUID.randomUUID(),
                creator = creator,
                card = card,
                collection = collection,
                min_prize = minPrize,
            )

        return auctionRepository.save(newAuction)
    }
}
