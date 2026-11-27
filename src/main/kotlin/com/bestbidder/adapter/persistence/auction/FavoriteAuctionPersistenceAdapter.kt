package com.bestbidder.adapter.persistence.auction

import com.bestbidder.adapter.persistence.user.JpaUserRepository
import com.bestbidder.domain.auction.FavoriteAuctionItem
import com.bestbidder.domain.auction.NotificationMode
import com.bestbidder.usecase.port.FavoriteAuctionRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
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
        notificationMode: NotificationMode,
    ) {
        if (jpaFavoriteRepository.existsByUserIdAndAuctionId(userId, auctionId)) {
            changeNotificationMode(userId, auctionId, notificationMode)
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
                notificationMode = notificationMode,
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

    @Transactional
    override fun changeNotificationMode(
        userId: UUID,
        auctionId: UUID,
        notificationMode: NotificationMode,
    ) {
        val updated = jpaFavoriteRepository.updateNotificationMode(userId, auctionId, notificationMode)
        require(updated > 0) { "La subasta $auctionId no está en favoritos del usuario $userId" }
    }

    @Transactional(readOnly = true)
    override fun getFavoritesByUserId(userId: UUID): List<FavoriteAuctionItem> {
        val now = Instant.now()
        return jpaFavoriteRepository.findFavoriteRowsByUserId(userId).map { it.toFavoriteItem(now) }
    }

    @Transactional(readOnly = true)
    override fun findUserIdsByAuctionId(auctionId: UUID): List<UUID> =
        jpaFavoriteRepository.findUserIdsByAuctionId(
            auctionId,
        )
}
