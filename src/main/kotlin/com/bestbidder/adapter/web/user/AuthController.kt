package com.bestbidder.adapter.web.user

import com.bestbidder.adapter.web.user.dto.ExistsResponse
import com.bestbidder.adapter.web.user.dto.LoginRequest
import com.bestbidder.adapter.web.user.dto.LoginResponse
import com.bestbidder.adapter.web.user.dto.RefreshRequest
import com.bestbidder.adapter.web.user.dto.RegisterRequest
import com.bestbidder.adapter.web.user.dto.UserResponse
import com.bestbidder.usecase.user.AuthUseCase
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authUseCase: AuthUseCase,
) {
    @PostMapping("/register")
    fun register(
        @Valid @RequestBody req: RegisterRequest,
    ): ResponseEntity<LoginResponse> {
        // Ahora email, username y password
        val result = authUseCase.register(req.email, req.username, req.password)
        return ResponseEntity.status(HttpStatus.CREATED).body(
            LoginResponse(
                accessToken = result.accessToken,
                refreshToken = result.refreshToken,
                user = UserResponse.fromDomain(result.user),
            ),
        )
    }

    @PostMapping("/login")
    fun login(
        @Valid @RequestBody req: LoginRequest,
    ): ResponseEntity<LoginResponse> {
        // Usamos el identifier
        val result = authUseCase.login(req.identifier, req.password)
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

    /**
     * Devuelve si un email ya está registrado en el sistema.
     * Útil para que el frontend avise antes de enviar el registro.
     */
    @GetMapping("/check-email")
    fun checkEmail(
        @RequestParam email: String,
    ): ResponseEntity<ExistsResponse> {
        return ResponseEntity.ok(ExistsResponse(authUseCase.emailExists(email)))
    }

    /**
     * Devuelve si un username ya está en uso.
     */
    @GetMapping("/check-username")
    fun checkUsername(
        @RequestParam username: String,
    ): ResponseEntity<ExistsResponse> {
        return ResponseEntity.ok(ExistsResponse(authUseCase.usernameExists(username)))
    }
}
