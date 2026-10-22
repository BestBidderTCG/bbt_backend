package com.bestbidder.adapter.persistence.auction

import com.bestbidder.domain.auction.Auction
import com.bestbidder.usecase.port.AuctionRepository
import org.springframework.stereotype.Component

@Component
class AuctionMapper(
    private val jpaAuctionRepository: JpaAuctionRepository,
) : AuctionRepository {
    override fun findById(id: String): Auction? {
        TODO("Not yet implemented")
    }

    override fun findByCreator(creator: String): List<Auction> {
        return jpaAuctionRepository.findByCreator(creator)
            .map { it.toDomain() }
    }

    override fun findByCollection(collection: String): List<Auction> {
        return jpaAuctionRepository.findByCollection(collection)
            .map { it.toDomain() }
    }

    override fun findByCard(card: String): List<Auction> {
        return jpaAuctionRepository.findByCard(card)
            .map { it.toDomain() }
    }

    override fun existsByCreatorAndCard(
        creator: String,
        card: String,
    ): Boolean {
        return jpaAuctionRepository.existsByCreatorAndCard(creator, card)
    }

    override fun save(auction: Auction): Auction {
        val entity = jpaAuctionRepository.save(auction.toEntity())
        return entity.toDomain()
    }

    private fun AuctionEntity.toDomain() =
        Auction(
            id = id,
            creator = creator,
            card = card,
            collection = collection,
            min_prize = min_prize,
        )

    private fun Auction.toEntity() =
        AuctionEntity(
            id = id,
            creator = creator,
            card = card,
            collection = collection,
            min_prize = min_prize,
        )
}
