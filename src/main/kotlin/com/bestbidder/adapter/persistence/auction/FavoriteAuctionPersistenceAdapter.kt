package com.bestbidder.adapter.persistence.auction

import com.bestbidder.adapter.persistence.user.JpaUserRepository
import com.bestbidder.domain.auction.Auction
import com.bestbidder.usecase.port.FavoriteAuctionRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Component
class FavoriteAuctionPersistenceAdapter(
    private val jpaFavoriteRepository: JpaFavoriteAuctionRepository,
    private val jpaUserRepository: JpaUserRepository,
    private val jpaAuctionRepository: JpaAuctionRepository,
) : FavoriteAuctionRepository {
    @Transactional
    override fun addFavorite(
        userId: UUID,
        auctionId: UUID,
    ) {
        if (jpaFavoriteRepository.existsByUserIdAndAuctionId(userId, auctionId)) {
            return
        }

        val user =
            jpaUserRepository.findById(userId)
                .orElseThrow { IllegalArgumentException("Usuario no encontrado: $userId") }

        val auction =
            jpaAuctionRepository.findById(auctionId)
                .orElseThrow { IllegalArgumentException("Subasta no encontrada: $auctionId") }

        jpaFavoriteRepository.save(
            FavoriteAuctionEntity(
                user = user,
                auction = auction,
            ),
        )
    }

    @Transactional
    override fun removeFavorite(
        userId: UUID,
        auctionId: UUID,
    ) {
        jpaFavoriteRepository.deleteByUserIdAndAuctionId(userId, auctionId)
    }

    override fun isFavorite(
        userId: UUID,
        auctionId: UUID,
    ): Boolean = jpaFavoriteRepository.existsByUserIdAndAuctionId(userId, auctionId)

    @Transactional(readOnly = true)
    override fun getFavoritesByUserId(userId: UUID): List<Auction> =
        jpaFavoriteRepository.findFavoriteAuctionsByUserId(userId).map { it.toDomain() }
}
