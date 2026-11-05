package com.bestbidder.adapter.web.user

import com.bestbidder.adapter.web.user.dto.ChangePasswordRequest
import com.bestbidder.adapter.web.user.dto.PublicUserResponse
import com.bestbidder.adapter.web.user.dto.UpdateUserProfileRequest
import com.bestbidder.adapter.web.user.dto.UserResponse
import com.bestbidder.infrastructure.security.AuthenticatedUser
import com.bestbidder.usecase.user.ChangePasswordUseCase
import com.bestbidder.usecase.user.GetUserUseCase
import com.bestbidder.usecase.user.UpdateUserUseCase
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/users")
class UserController(
    private val getUserUseCase: GetUserUseCase,
    private val updateUserUseCase: UpdateUserUseCase,
    private val changePasswordUseCase: ChangePasswordUseCase,
) {
    private fun getCurrentUser(): AuthenticatedUser {
        return SecurityContextHolder.getContext().authentication.principal as AuthenticatedUser
    }

    @GetMapping("/me")
    fun me(): UserResponse {
        val principal = getCurrentUser()
        return UserResponse.fromDomain(getUserUseCase.getById(principal.id))
    }

    @GetMapping("/{id}")
    fun byId(
        @PathVariable id: UUID,
    ): PublicUserResponse = PublicUserResponse.fromDomain(getUserUseCase.getById(id))

    @PatchMapping("/me")
    fun update(
        @RequestBody req: UpdateUserProfileRequest,
    ): UserResponse {
        val principal = getCurrentUser()
        val updated =
            updateUserUseCase.updateProfile(
                userId = principal.id,
                description = req.description,
                avatarNum = req.avatarNum,
            )
        return UserResponse.fromDomain(updated)
    }

    @PatchMapping("/me/password")
    fun changePassword(
        @Valid @RequestBody req: ChangePasswordRequest,
    ): ResponseEntity<Void> {
        val principal = getCurrentUser()
        changePasswordUseCase.change(principal.id, req.currentPassword, req.newPassword)
        return ResponseEntity.noContent().build()
    }
}
