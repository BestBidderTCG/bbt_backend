package com.bestbidder.adapter.persistence.auction

import com.bestbidder.domain.auction.Auction
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "auctions")
class AuctionEntity(
    @Id
    val id: UUID = UUID.randomUUID(),
    val creator: String,
    val card: String,
    val collection: String,
    val minPrize: Double,
) {
    fun toDomain(): Auction =
        Auction(
            id = id,
            creator = creator,
            card = card,
            collection = collection,
            min_prize = minPrize,
        )

    companion object {
        fun fromDomain(auction: Auction): AuctionEntity =
            AuctionEntity(
                id = auction.id,
                creator = auction.creator,
                card = auction.card,
                collection = auction.collection,
                minPrize = auction.min_prize,
            )
    }
}
