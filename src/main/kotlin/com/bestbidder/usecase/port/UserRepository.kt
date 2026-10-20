package com.bestbidder.usecase.port

import com.bestbidder.domain.user.User

interface UserRepository {
    fun findByUsername(username: String): User?
    fun save(user: User): User
}