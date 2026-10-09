package com.availelabs.tabla

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class TickerIngestionJob(
    private val dataIngestor: DataIngestor
) {
    @Scheduled(cron = "0 0 3 * * *", zone = "America/New_York")
    fun run() {
        dataIngestor.ingestTickers()
    }
}
