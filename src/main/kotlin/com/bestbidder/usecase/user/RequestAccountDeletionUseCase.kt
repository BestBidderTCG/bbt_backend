package com.bestbidder.usecase.user

import com.bestbidder.domain.user.User
import com.bestbidder.domain.user.exceptions.AccountDeletionBlockedException
import com.bestbidder.domain.user.exceptions.UserNotFoundException
import com.bestbidder.infrastructure.security.RefreshTokenService
import com.bestbidder.usecase.port.AuctionRepository
import com.bestbidder.usecase.port.BidRepository
import com.bestbidder.usecase.port.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class RequestAccountDeletionUseCase(
    private val userRepository: UserRepository,
    private val auctionRepository: AuctionRepository,
    private val bidRepository: BidRepository,
    private val refreshTokenService: RefreshTokenService,
) {
    @Transactional
    fun execute(userId: UUID): User {
        val user = userRepository.findById(userId)

        // Las dos comprobaciones en una sola para que Detekt esté contento
        if (user == null || user.deletedAt != null) {
            throw UserNotFoundException(userId)
        }

        // Estado actual
        if (user.deletionRequestedAt != null) return user

        val blockers = mutableListOf<String>()
        if (auctionRepository.hasActiveAuctionsAsCreator(userId)) {
            blockers.add("tienes subastas activas o pendientes de comenzar")
        }
        if (bidRepository.isHighestBidderInActiveAuction(userId)) {
            blockers.add("eres el pujador más alto en una subasta en curso")
        }
        if (blockers.isNotEmpty()) {
            throw AccountDeletionBlockedException(blockers)
        }

        val updated = userRepository.save(user.copy(deletionRequestedAt = Instant.now()))
        refreshTokenService.revokeAll(userId)
        return updated
    }
}
