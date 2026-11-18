package com.bestbidder.adapter.web.user.dto

data class AccountDeletionCheckResponse(
    val canDelete: Boolean,
    val blockers: List<String>,
)
