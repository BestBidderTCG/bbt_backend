package com.bestbidder.domain.user.exceptions

class InvalidRefreshTokenException :
    RuntimeException("Refresh token inválido o expirado")
