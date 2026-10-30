package com.bestbidder.usecase.user

import com.bestbidder.domain.user.User
import com.bestbidder.domain.user.exceptions.UserNotFoundException
import com.bestbidder.usecase.port.UserRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class GetUserUseCase(
    private val userRepository: UserRepository,
) {
    fun getById(id: UUID): User = userRepository.findById(id) ?: throw UserNotFoundException(id)
}
