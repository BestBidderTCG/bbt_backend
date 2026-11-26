package com.bestbidder.usecase.user

import com.bestbidder.domain.user.User
import com.bestbidder.domain.user.UserStats
import com.bestbidder.domain.user.exceptions.UserNotFoundException
import com.bestbidder.usecase.port.AuctionRepository
import com.bestbidder.usecase.port.BidRepository
import com.bestbidder.usecase.port.CardRepository
import com.bestbidder.usecase.port.UserRepository
import org.springframework.stereotype.Service
import java.util.UUID

data class UserProfile(
    val user: User,
    val stats: UserStats,
)

@Service
class GetUserUseCase(
    private val userRepository: UserRepository,
    private val auctionRepository: AuctionRepository,
    private val bidRepository: BidRepository,
    private val cardRepository: CardRepository,
) {
    fun getById(id: UUID): User {
        val user = userRepository.findById(id) ?: throw UserNotFoundException(id)
        if (user.deletedAt != null) throw UserNotFoundException(id)
        return user
    }

    fun getProfile(id: UUID): UserProfile {
        val user = getById(id)
        val stats =
            UserStats(
                auctionCount = auctionRepository.countByCreatorId(id),
                bidCount = bidRepository.countByBidderId(id),
                cardCount = cardRepository.countByOwnerId(id),
            )
        return UserProfile(user, stats)
    }
}
