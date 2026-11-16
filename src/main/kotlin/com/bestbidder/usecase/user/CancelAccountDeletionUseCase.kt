package com.bestbidder.usecase.user

import com.bestbidder.domain.user.exceptions.UserNotFoundException
import com.bestbidder.usecase.port.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class CancelAccountDeletionUseCase(
    private val userRepository: UserRepository,
) {
    @Transactional
    fun execute(userId: UUID) {
        val user = userRepository.findById(userId) ?: throw UserNotFoundException(userId)
        if (user.deletionRequestedAt == null) return
        userRepository.save(user.copy(deletionRequestedAt = null))
    }
}
