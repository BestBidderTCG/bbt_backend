package com.bestbidder.domain.user.exceptions

class UsernameAlreadyExistsException(username: String) :
    RuntimeException("El nombre de usuario '$username' ya está en uso")
