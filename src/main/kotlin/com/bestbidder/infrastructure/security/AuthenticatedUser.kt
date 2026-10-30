package com.bestbidder.infrastructure.security

import java.util.UUID

data class AuthenticatedUser(val id: UUID, val username: String)
