package com.bestbidder.adapter.persistence.aution

import com.bestbidder.adapter.persistence.auction.AuctionEntity
import com.bestbidder.adapter.persistence.auction.JpaAuctionRepository
import com.bestbidder.domain.auction.Auction
import com.bestbidder.usecase.port.AuctionRepository
import org.springframework.stereotype.Component

/**
 * Adaptador que implementa la interfaz.
 * Se encarga de conectar la lógica con la base de datos real.
 * También actúa como "Traductor" (Mapper) entre [AuctionEntity] (DB) y [Auction] (Dominio).
 */
@Component
class AuctionMapper(
    private val jpaAuctionRepository: JpaAuctionRepository,
) : AuctionRepository {
    override fun findById(id: String): Auction? {
        TODO("Not yet implemented")
    }

    override fun findByCreator(creator: String): List<Auction> {
        TODO("Not yet implemented")
    }

    override fun findByCollection(cart: String): List<Auction> {
        TODO("Not yet implemented")
    }

    override fun findByCart(cart: String): List<Auction> {
        TODO("Not yet implemented")
    }

    override fun save(auction: Auction): Auction {
        val entity = jpaAuctionRepository.save(auction.toEntity())
        return entity.toDomain()
    }

    // Mappers
    private fun AuctionEntity.toDomain() = Auction(id, creator, cart, collection, min_prize)

    private fun Auction.toEntity() = AuctionEntity(id, creator, cart, collection, min_prize)
}
