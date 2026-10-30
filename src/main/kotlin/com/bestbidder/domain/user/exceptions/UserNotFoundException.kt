package com.bestbidder.domain.user.exceptions

import java.util.UUID

class UserNotFoundException(id: UUID) :
    RuntimeException("Usuario no encontrado: $id")
