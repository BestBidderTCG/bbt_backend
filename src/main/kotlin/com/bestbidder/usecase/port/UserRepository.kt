package com.bestbidder.usecase.port

import com.bestbidder.domain.user.User
import java.util.UUID

interface UserRepository {
    fun findByUsername(username: String): User?

    fun findById(id: UUID): User?

    fun existsByUsername(username: String): Boolean

    fun save(user: User): User

    fun existsByEmail(email: String): Boolean

    fun findByEmail(email: String): User?
}
