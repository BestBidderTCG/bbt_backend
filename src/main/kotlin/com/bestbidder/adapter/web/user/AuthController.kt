package com.bestbidder.adapter.web.user

import com.bestbidder.adapter.web.user.dto.AuthRequest
import com.bestbidder.adapter.web.user.dto.LoginResponse
import com.bestbidder.adapter.web.user.dto.RefreshRequest
import com.bestbidder.adapter.web.user.dto.UserResponse
import com.bestbidder.usecase.user.AuthUseCase
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authUseCase: AuthUseCase,
) {
    @PostMapping("/register")
    fun register(
        @Valid @RequestBody req: AuthRequest,
    ): ResponseEntity<UserResponse> {
        val user = authUseCase.register(req.username, req.password)
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.fromDomain(user))
    }

    @PostMapping("/login")
    fun login(
        @Valid @RequestBody req: AuthRequest,
    ): ResponseEntity<LoginResponse> {
        val result = authUseCase.login(req.username, req.password)
        return ResponseEntity.ok(
            LoginResponse(result.accessToken, result.refreshToken, UserResponse.fromDomain(result.user)),
        )
    }

    @PostMapping("/refresh")
    fun refresh(
        @Valid @RequestBody req: RefreshRequest,
    ): ResponseEntity<LoginResponse> {
        val result = authUseCase.refresh(req.refreshToken)
        return ResponseEntity.ok(
            LoginResponse(result.accessToken, result.refreshToken, UserResponse.fromDomain(result.user)),
        )
    }

    @PostMapping("/logout")
    fun logout(
        @Valid @RequestBody req: RefreshRequest,
    ): ResponseEntity<Void> {
        authUseCase.logout(req.refreshToken)
        return ResponseEntity.noContent().build()
    }
}
