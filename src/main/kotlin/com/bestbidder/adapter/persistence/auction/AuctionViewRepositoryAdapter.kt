package com.bestbidder.adapter.persistence.auction

import com.bestbidder.usecase.port.AuctionViewRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Instant
import java.util.UUID

@Repository
class AuctionViewRepositoryAdapter(
    private val jpaAuctionViewRepository: JpaAuctionViewRepository,
    private val jpaAuctionRepository: JpaAuctionRepository,
    @Value("\${bestbidder.auction.view-window-hours:10}")
    private val windowHours: Long,
) : AuctionViewRepository {
    @Transactional
    override fun registerView(
        auctionId: UUID,
        userId: UUID,
        now: Instant,
    ): Boolean {
        val threshold = now.minus(Duration.ofHours(windowHours))
        val counted =
            jpaAuctionViewRepository.refreshIfWindowElapsed(userId, auctionId, now, threshold) > 0 ||
                jpaAuctionViewRepository.insertIfAbsent(userId, auctionId, now) > 0

        if (counted) {
            jpaAuctionRepository.incrementViewCount(auctionId)
        }
        return counted
    }
}
