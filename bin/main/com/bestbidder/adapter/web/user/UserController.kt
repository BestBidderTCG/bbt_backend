package com.bestbidder.adapter.web.user

import com.bestbidder.adapter.web.user.dto.AuthRequest
import com.bestbidder.adapter.web.user.dto.UserResponse
import com.bestbidder.usecase.user.AuthUseCase
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Controlador REST que expone los endpoints realizando la autenticación de usuarios.
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = ["*"])
class UserController(
    private val authUseCase: AuthUseCase,
) {
    @PostMapping("/register")
    fun register(
        @RequestBody request: AuthRequest,
    ): ResponseEntity<UserResponse> {
        val user = authUseCase.register(request.username, request.password)
        return ResponseEntity.ok(UserResponse(user.id, user.username))
    }

    @PostMapping("/login")
    fun login(
        @RequestBody request: AuthRequest,
    ): ResponseEntity<UserResponse> {
        val user = authUseCase.login(request.username, request.password)
        return ResponseEntity.ok(UserResponse(user.id, user.username))
    }
}
