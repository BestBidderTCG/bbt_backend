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
import org.springframework.security.core.annotation.AuthenticationPrincipal
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
    @GetMapping("/me")
    fun me(
        @AuthenticationPrincipal principal: AuthenticatedUser,
    ): UserResponse = UserResponse.fromDomain(getUserUseCase.getById(principal.id))

    @GetMapping("/{id}")
    fun byId(
        @PathVariable id: UUID,
    ): PublicUserResponse = PublicUserResponse.fromDomain(getUserUseCase.getById(id))

    @PatchMapping("/me")
    fun update(
        @AuthenticationPrincipal principal: AuthenticatedUser,
        @RequestBody req: UpdateUserProfileRequest,
    ): UserResponse {
        val updated =
            updateUserUseCase.updateProfile(
                userId = principal.id,
                displayName = req.displayName,
                description = req.description,
                avatarNum = req.avatarNum,
            )
        return UserResponse.fromDomain(updated)
    }

    @PatchMapping("/me/password")
    fun changePassword(
        @AuthenticationPrincipal principal: AuthenticatedUser,
        @Valid @RequestBody req: ChangePasswordRequest,
    ): ResponseEntity<Void> {
        changePasswordUseCase.change(principal.id, req.currentPassword, req.newPassword)
        return ResponseEntity.noContent().build()
    }
}
