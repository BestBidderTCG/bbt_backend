package com.bestbidder.infrastructure.scheduler

import com.bestbidder.usecase.user.PurgeDeletedAccountsUseCase
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class AccountScheduler(
    private val purgeDeletedAccountsUseCase: PurgeDeletedAccountsUseCase,
) {
    @Scheduled(cron = "0 0 3 * * *") // Diario a las 03:00
    fun purgeAccounts() {
        purgeDeletedAccountsUseCase.execute(Instant.now())
    }
}
