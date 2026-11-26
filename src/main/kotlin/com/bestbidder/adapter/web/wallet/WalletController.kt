package com.bestbidder.adapter.web.wallet

import com.bestbidder.adapter.web.wallet.dto.BalanceResponse
import com.bestbidder.adapter.web.wallet.dto.TopUpRequest
import com.bestbidder.adapter.web.wallet.dto.WalletTransactionResponse
import com.bestbidder.infrastructure.security.AuthenticatedUser
import com.bestbidder.usecase.user.GetUserUseCase
import com.bestbidder.usecase.wallet.GetTransactionHistoryUseCase
import com.bestbidder.usecase.wallet.TopUpBalanceCommand
import com.bestbidder.usecase.wallet.TopUpBalanceUseCase
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/wallet")
class WalletController(
    private val getUserUseCase: GetUserUseCase,
    private val topUpBalanceUseCase: TopUpBalanceUseCase,
    private val getTransactionHistoryUseCase: GetTransactionHistoryUseCase,
) {
    private fun getCurrentUser(): AuthenticatedUser {
        val auth = SecurityContextHolder.getContext().authentication
        return auth.principal as AuthenticatedUser
    }

    @GetMapping("/balance")
    fun balance(): BalanceResponse = BalanceResponse(getUserUseCase.getById(getCurrentUser().id).balance)

    @PostMapping("/top-up")
    fun topUp(
        @Valid @RequestBody req: TopUpRequest,
    ): ResponseEntity<WalletTransactionResponse> {
        val transaction =
            topUpBalanceUseCase.execute(
                TopUpBalanceCommand(userId = getCurrentUser().id, amount = req.amount),
            )
        return ResponseEntity.status(HttpStatus.CREATED).body(WalletTransactionResponse.fromDomain(transaction))
    }

    @GetMapping("/transactions")
    fun transactions(): List<WalletTransactionResponse> =
        getTransactionHistoryUseCase.execute(getCurrentUser().id).map { WalletTransactionResponse.fromDomain(it) }
}
