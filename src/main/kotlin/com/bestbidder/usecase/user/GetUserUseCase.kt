package com.bestbidder.usecase.user

import com.bestbidder.domain.user.User
import com.bestbidder.domain.user.exceptions.UserNotFoundException
import com.bestbidder.usecase.port.UserRepository
import org.springframework.stereotype.Service
import java.util.UUID

data class UserProfile(
    val user: User,
)

@Service
class GetUserUseCase(
    private val userRepository: UserRepository,
) {
    fun getById(id: UUID): User {
        val user = userRepository.findById(id) ?: throw UserNotFoundException(id)
        if (user.deletedAt != null) throw UserNotFoundException(id)
        return user
    }
}
