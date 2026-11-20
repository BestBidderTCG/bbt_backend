package com.bestbidder.usecase.auction

import com.bestbidder.domain.auction.Auction
import com.bestbidder.domain.auction.AuctionDetail
import com.bestbidder.domain.auction.AuctionListItem
import com.bestbidder.domain.auction.AuctionSummary
import com.bestbidder.usecase.port.AuctionRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class GetAuctionUseCase(
    private val auctionRepository: AuctionRepository,
) {
    fun getById(id: UUID): Auction? {
        return auctionRepository.getById(id)
    }

    fun getDetail(
        id: UUID,
        viewerId: UUID?,
    ): AuctionDetail? {
        return auctionRepository.getDetailById(id, viewerId)
    }

    fun getAll(sort: AuctionSort = AuctionSort.POPULARITY): List<AuctionListItem> {
        return auctionRepository.getAllSummaries(sort)
    }

    fun getUserAuctions(id: UUID): List<AuctionSummary> {
        return auctionRepository.getUserAuctions(id)
    }
}
