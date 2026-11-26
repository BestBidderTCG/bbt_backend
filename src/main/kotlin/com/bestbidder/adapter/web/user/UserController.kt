package com.bestbidder.adapter.web.user

import com.bestbidder.adapter.web.user.dto.AccountDeletionCheckResponse
import com.bestbidder.adapter.web.user.dto.ChangePasswordRequest
import com.bestbidder.adapter.web.user.dto.DeletionStatusResponse
import com.bestbidder.adapter.web.user.dto.PublicUserResponse
import com.bestbidder.adapter.web.user.dto.UpdateUserProfileRequest
import com.bestbidder.adapter.web.user.dto.UserResponse
import com.bestbidder.infrastructure.security.AuthenticatedUser
import com.bestbidder.usecase.user.CancelAccountDeletionUseCase
import com.bestbidder.usecase.user.ChangePasswordUseCase
import com.bestbidder.usecase.user.CheckAccountDeletionUseCase
import com.bestbidder.usecase.user.GetUserUseCase
import com.bestbidder.usecase.user.RequestAccountDeletionUseCase
import com.bestbidder.usecase.user.UpdateUserUseCase
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/users")
@Suppress("LongParameterList")
class UserController(
    private val getUserUseCase: GetUserUseCase,
    private val updateUserUseCase: UpdateUserUseCase,
    private val changePasswordUseCase: ChangePasswordUseCase,
    private val requestAccountDeletionUseCase: RequestAccountDeletionUseCase,
    private val cancelAccountDeletionUseCase: CancelAccountDeletionUseCase,
    private val checkAccountDeletionUseCase: CheckAccountDeletionUseCase,
) {
    private fun getCurrentUser(): AuthenticatedUser {
        return SecurityContextHolder.getContext().authentication.principal as AuthenticatedUser
    }

    @GetMapping("/me")
    fun me(): UserResponse {
        val profile = getUserUseCase.getById(getCurrentUser().id)
        return UserResponse.fromDomain(profile)
    }

    @GetMapping("/{id}")
    fun byId(
        @PathVariable id: UUID,
    ): PublicUserResponse {
        val profile = getUserUseCase.getById(id)
        return PublicUserResponse.fromDomain(profile)
    }

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

    @DeleteMapping("/me")
    fun requestDeletion(): ResponseEntity<DeletionStatusResponse> {
        val principal = getCurrentUser()
        val user = requestAccountDeletionUseCase.execute(principal.id)
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(DeletionStatusResponse.from(user))
    }

    @PostMapping("/me/cancel-deletion")
    fun cancelDeletion(): ResponseEntity<Void> {
        val principal = getCurrentUser()
        cancelAccountDeletionUseCase.execute(principal.id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/me/deletion-status")
    fun deletionStatus(): DeletionStatusResponse {
        val principal = getCurrentUser()
        return DeletionStatusResponse.from(getUserUseCase.getById(principal.id))
    }

    @GetMapping("/me/deletion-check")
    fun checkDeletion(): AccountDeletionCheckResponse {
        val principal = getCurrentUser()
        val result = checkAccountDeletionUseCase.execute(principal.id)
        return AccountDeletionCheckResponse(
            hasActiveAuction = result.hasActiveAuction,
            isWinningAuction = result.isWinningAuction,
        )
    }
}
