package com.bestbidder.infrastructure.scheduler

import com.bestbidder.usecase.auction.CloseExpiredAuctionsUseCase
import com.bestbidder.usecase.auction.StartPendingAuctionsUseCase
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.temporal.ChronoUnit

@Component
class AuctionScheduler(
    private val startPendingAuctionsUseCase: StartPendingAuctionsUseCase,
    private val closeExpiredAuctionsUseCase: CloseExpiredAuctionsUseCase,
) {
    @Scheduled(cron = "0 0,15,30,45 * * * *") // Se despierta a las :00, :15, :30, :45
    fun processAuctionTransitions() {
        val cleanNow = Instant.now().truncatedTo(ChronoUnit.MINUTES)

        startPendingAuctionsUseCase.execute(cleanNow)
        closeExpiredAuctionsUseCase.execute(cleanNow)
    }
}
