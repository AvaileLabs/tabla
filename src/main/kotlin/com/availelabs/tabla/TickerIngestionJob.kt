package com.availelabs.tabla

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class TickerIngestionJob(
    private val tickerIngestor: TickerIngestor
) : ApplicationRunner {
    override fun run(args: ApplicationArguments) {
        tickerIngestor.ingest()
    }

    @Scheduled(cron = "0 0 3 * * *", zone = "America/New_York")
    fun runDaily() {
        tickerIngestor.ingest()
    }
}
