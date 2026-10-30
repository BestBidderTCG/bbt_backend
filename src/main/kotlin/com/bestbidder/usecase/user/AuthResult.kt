package com.bestbidder.usecase.user

import com.bestbidder.domain.user.User

data class AuthResult(
    val user: User,
    val accessToken: String,
    val refreshToken: String,
)
