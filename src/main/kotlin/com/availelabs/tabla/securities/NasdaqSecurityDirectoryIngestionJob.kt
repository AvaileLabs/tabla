package com.availelabs.tabla.securities

import com.availelabs.tabla.securities.persistence.SecurityListingRepository
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.security.SecureRandom
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId

@Component
class NasdaqSecurityDirectoryIngestionJob(
    private val nasdaqSecurityDirectoryIngestor: NasdaqSecurityDirectoryIngestor,
    private val repository: SecurityListingRepository,
    @Suppress("SpringJavaInjectionPointsAutowiringInspection") // file report later
    private val taskScheduler: TaskScheduler
) : ApplicationRunner {

    private val zone = ZoneId.of("America/New_York")
    private val random = SecureRandom()

    override fun run(args: ApplicationArguments) {
        ingest()
    }

    @Scheduled(cron = "@midnight", zone = "America/New_York")
    fun runDaily() {
        val midnight = LocalDate.now(zone).atStartOfDay(zone)
        val window = Duration.between(midnight, midnight.withHour(5)).toMillis()

        taskScheduler.schedule(
            { ingest() },
            midnight.toInstant().plusMillis(random.nextLong(window))
        )
    }

    private fun ingest() {
        repository.replaceNasdaqDirectory(nasdaqSecurityDirectoryIngestor.ingest())
    }
}
