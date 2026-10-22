package com.bestbidder.usecase.auction

import com.bestbidder.domain.auction.Auction
import com.bestbidder.usecase.port.AuctionRepository
import org.springframework.stereotype.Service

@Service
class FindAuctionUseCase(
    private val auctionRepository: AuctionRepository,
) {
    fun getAll(): List<Auction> {
        return auctionRepository.findAll()
    }

    fun getByCreator(creator: String): List<Auction> {
        return auctionRepository.findByCreator(creator)
    }
}
