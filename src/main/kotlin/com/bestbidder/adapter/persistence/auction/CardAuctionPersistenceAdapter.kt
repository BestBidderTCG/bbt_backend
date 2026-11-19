package com.bestbidder.adapter.persistence.auction

import com.bestbidder.usecase.port.ActiveCardAuction
import com.bestbidder.usecase.port.CardAuctionPort
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID

@Component
class CardAuctionPersistenceAdapter(
    private val jpaAuctionRepository: JpaAuctionRepository,
) : CardAuctionPort {
    override fun findActiveOrUpcomingByCardId(cardId: UUID): ActiveCardAuction? {
        val now = Instant.now()
        val auction = jpaAuctionRepository.findActiveOrUpcomingByCardId(cardId, now) ?: return null

        val status = if (auction.startedAt.isAfter(now)) "UPCOMING" else "IN_PROGRESS"
        return ActiveCardAuction(
            id = auction.id,
            status = status,
        )
    }
}
