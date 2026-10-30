package com.bestbidder.usecase.auction

import com.bestbidder.domain.auction.Auction
import com.bestbidder.usecase.port.AuctionRepository
import org.springframework.stereotype.Service

@Service
class GetAuctionUseCase(
    private val auctionRepository: AuctionRepository,
) {
    fun getAll(): List<Auction> {
        return auctionRepository.getAll()
    }

    fun getActive(): List<Auction> {
        return auctionRepository.getActive()
    }

    fun getByCreator(creator: String): List<Auction> {
        return auctionRepository.getByCreator(creator)
    }

    fun getByCollection(collection: String): List<Auction> {
        return auctionRepository.getByCollection(collection)
    }

    fun getByCard(card: String): List<Auction> {
        return auctionRepository.getByCard(card)
    }
}
