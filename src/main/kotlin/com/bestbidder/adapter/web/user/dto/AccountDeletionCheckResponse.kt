package com.bestbidder.adapter.web.user.dto

data class AccountDeletionCheckResponse(
    val hasActiveAuction: Boolean,
    val isWinningAuction: Boolean,
)
