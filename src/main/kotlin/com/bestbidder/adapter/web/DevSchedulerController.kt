package com.bestbidder.adapter.web

import com.bestbidder.usecase.auction.CloseExpiredAuctionsUseCase
import com.bestbidder.usecase.auction.StartPendingAuctionsUseCase
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
@RequestMapping("/api/dev/scheduler")
@Profile("!prod")
class DevSchedulerController(
    private val startPendingAuctionsUseCase: StartPendingAuctionsUseCase,
    private val closeExpiredAuctionsUseCase: CloseExpiredAuctionsUseCase,
) {
    @PostMapping("/start")
    fun triggerStart(
        @RequestParam(required = false) at: Instant?,
    ) {
        startPendingAuctionsUseCase.execute(at ?: Instant.now())
    }

    @PostMapping("/close")
    fun triggerClose(
        @RequestParam(required = false) at: Instant?,
    ) {
        closeExpiredAuctionsUseCase.execute(at ?: Instant.now())
    }
}
