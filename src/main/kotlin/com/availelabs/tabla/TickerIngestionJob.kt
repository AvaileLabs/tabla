package com.availelabs.tabla

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class TickerIngestionJob(
    private val dataIngestor: DataIngestor
) : ApplicationRunner {
    override fun run(args: ApplicationArguments) {
        dataIngestor.ingestTickers()
    }

    @Scheduled(cron = "0 0 3 * * *", zone = "America/New_York")
    fun runDaily() {
        dataIngestor.ingestTickers()
    }
}
