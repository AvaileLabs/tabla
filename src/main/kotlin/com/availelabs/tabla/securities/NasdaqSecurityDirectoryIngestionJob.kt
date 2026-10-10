package com.availelabs.tabla.securities

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class NasdaqSecurityDirectoryIngestionJob(
    private val nasdaqSecurityDirectoryIngestor: NasdaqSecurityDirectoryIngestor
) : ApplicationRunner {
    override fun run(args: ApplicationArguments) {
//        nasdaqSecurityDirectoryIngestor.ingest()
    }

    @Scheduled(cron = "0 0 3 * * *", zone = "America/New_York")
    fun runDaily() {
        nasdaqSecurityDirectoryIngestor.ingest()
    }
}
